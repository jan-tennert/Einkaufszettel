package io.github.jan.einkaufszettel.desktop

import io.github.jan.einkaufszettel.common.controller.AuthController
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.common.repositories.ProfileRepository
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.einkaufszettel.common.repositories.card.Card
import io.github.jan.einkaufszettel.common.repositories.card.CardRepository
import io.github.jan.einkaufszettel.common.repositories.product.ProductRepository
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.repositories.product.User
import io.github.jan.einkaufszettel.common.repositories.shop.ShopRepository
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.providers.Email
import io.github.jan.supacompose.auth.providers.Google
import io.github.jan.supacompose.auth.providers.htmlText
import io.github.jan.supacompose.auth.providers.htmlTitle
import io.github.jan.supacompose.exceptions.RestException
import io.github.jan.supacompose.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.net.UnknownHostException

class ProductViewModel : KoinComponent, AuthController {

    private val scope = CoroutineScope(Dispatchers.IO)

    val supabaseClient: SupabaseClient by inject()
    val productRepository: ProductRepository by inject()
    val profileRepository: ProfileRepository by inject()
    val cardRepository: CardRepository by inject()
    val shopRepository: ShopRepository by inject()
    val httpClient: HttpClient by inject()

    val profileFlow = MutableStateFlow<UserStatus>(UserStatus.NotTried)
    val shopFlow = MutableStateFlow<List<Shop>>(emptyList())
    val cardFlow = MutableStateFlow<List<Card>>(emptyList())
    val knownUsers = MutableStateFlow(emptyList<User>())
    val eventFlow = MutableStateFlow<List<UIEvent>>(emptyList())
    val settings = MutableStateFlow(Settings())
    val versionFlow = MutableStateFlow(-1)

    private val settingsFile = File(File(System.getProperty("user.home"), "Einkaufszettel"),"settings.json")
    private val knownUsersFile = File(File(System.getProperty("user.home"), "Einkaufszettel"),"knownUsers.json")

    fun loadProfile(id: String, first: Boolean) {
        if(first) {
            profileFlow.value = UserStatus.Loading
        }
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                profileRepository.getProfile(id)
            }.onSuccess {
                println(it)
                if(it != null) {
                    profileFlow.value = UserStatus.Success(it)
                } else {
                    profileFlow.value = UserStatus.NotFound
                }
            }.onFailure {
                it.printStackTrace()
                when(it) {
                    is UnknownHostException -> UserStatus.Error
                    else -> profileFlow.value = UserStatus.Error
                }
            }
        }
    }

    fun createProfile(id: String, username: String) {
        profileFlow.value = UserStatus.Loading
        scope.launch {
            kotlin.runCatching {
                profileRepository.createProfile(id, username)
            }.onSuccess {
                profileFlow.value = UserStatus.Success(it)
            }.onFailure {
                it.printStackTrace()
                profileFlow.value = UserStatus.NotFound
                pushEvent(UIEvent.AlertEvent("Fehler beim Erstellen des Profils. Bitte überprüfe deine Internetverbindung."))
            }
        }
    }

    override fun signUpWithEmail(email: String, password: String, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onSuccess {
                pushEvent(UIEvent.AlertEvent("Registrierung erfolgreich. Bitte überprüfe deine E-Mails, um die Registrierung abzuschließen."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Registrierung fehlgeschlagen. Bitte überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    override fun loginWithEmail(email: String, password: String, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.loginWith(Email) {
                    this.email = email
                    this.password = password
                }
            }.onFailure {
                it.printStackTrace()
                when(it) {
                    is RestException -> pushEvent(UIEvent.AlertEvent("Login fehlgeschlagen. Bitte überprüfe deine Anmeldedaten."))
                    else -> pushEvent(UIEvent.AlertEvent("Login fehlgeschlagen. Bitte überprüfe deine Internetverbindung."))
                }
            }
            callback()
        }
    }

    override fun loginWithGoogle() {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.loginWith(Google) {
                    htmlText = "Du wurdest eingeloggt. Du kannst im Programm weitermachen."
                    htmlTitle = "Einkaufszettel"
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun changePasswordTo(password: String) {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.modifyUser(Email) {
                    this.password = password
                }
            }.onSuccess {
                pushEvent(UIEvent.AlertEvent("Passwort erfolgreich geändert."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Konnte Passwort nicht ändern. Bitte überprüfe deine Internetverbindung."))
            }
        }
    }

    fun createShop(
        name: String,
        icon: File,
        authorizedUsers: List<String>,
        callback: () -> Unit
    ) {
        scope.launch {
            kotlin.runCatching {
                val key = shopRepository.uploadIcon(icon.extension, icon.readBytes())
                shopRepository.createShop(name, supabaseClient.path("storage/v1/object/public/$key"), supabaseClient.auth.currentSession.value!!.user!!.id, authorizedUsers)
            }.onSuccess {
                refreshProducts()
                pushEvent(UIEvent.AlertEvent("Shop wurde erstellt."))
             //   productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    fun editShop(shop: Int, newName: String, authorizedUsers: List<String>, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                shopRepository.editShop(shop, newName, authorizedUsers)
            }.onSuccess {
                refreshProducts()
                pushEvent(UIEvent.AlertEvent("Shop wurde bearbeitet."))
             //   productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    fun deleteShop(id: Int) {
        scope.launch {
            kotlin.runCatching {
                shopRepository.deleteShop(id)
            }.onSuccess {
                refreshProducts()
                pushEvent(UIEvent.AlertEvent("Shop wurde gelöscht."))
             //   productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun updateUsername(id: String, name: String, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                profileRepository.updateUsername(id, name)
                pushEvent(UIEvent.AlertEvent("Profil erfolgreich gespeichert."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Konnte Profil nicht speichern. Bitte überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    fun getLatestVersion() {
        scope.launch {
            kotlin.runCatching {
                httpClient.get("http://xowugqynwpspvhno.myfritz.net:26666/api/shopping/version").body<JsonObject>()["version"]!!.jsonPrimitive.int
            }.onSuccess {
                println(it)
                if(versionFlow.value != -2) {
                    versionFlow.value = it
                }
            }.onFailure {
                versionFlow.value = -1
            }
        }
    }

    fun logout() {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.invalidateSession()
                profileFlow.value = UserStatus.NotFound
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun sendPasswordRecovery(email: String) {
        scope.launch {
            kotlin.runCatching {
                supabaseClient.auth.sendRecoveryEmail(email)
            }.onSuccess {
                pushEvent(UIEvent.AlertEvent("Passwort-Wiederherstellungs-Link wurde an deine E-Mail gesendet."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Passwort-Wiederherstellung fehlgeschlagen. Bitte überprüfe deine Internetverbindung."))
            }
        }
    }

    fun refreshProducts(callback: () -> Unit = {}) {
        scope.launch {
            kotlin.runCatching {
                productRepository.getShops()
            }.onSuccess {
                shopFlow.value = it
                //lastRefreshFlow.value = DateTime.nowLocal()
            //    updateOrder(context)
             //   connectionStatus.value = true
                callback()
            }.onFailure {
                it.printStackTrace()
              //  connectionStatus.value = false
            }
        }
    }

    fun createProductInShop(shop: Int, content: String, creatorId: String) {
        scope.launch {
            kotlin.runCatching {
                productRepository.createInShop(shop, content, creatorId)
            }.onSuccess {
                refreshProducts()
            }.onFailure {
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun markProductAsDone(product: Int, userId: String, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                productRepository.markAsDone(product, userId)
            }.onSuccess {
                refreshProducts(callback)
            }.onFailure {
                it.printStackTrace()
                callback()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht als erledigt markiert werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun markProductAsNotDone(product: Int, callback: () -> Unit) {
        scope.launch {
            kotlin.runCatching {
                productRepository.markAsNotDone(product)
            }.onSuccess {
                refreshProducts(callback)
            }.onFailure {
                it.printStackTrace()
                callback()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht als erledigt markiert werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun loadKnownUsers() {
        scope.launch(Dispatchers.IO) {
            val file = knownUsersFile
            if(!file.exists()) {
                file.createNewFile()
                file.writeText("[]")
                return@launch
            } else {
                knownUsers.value = Json.decodeFromString(file.readText())
            }
        }
    }

    fun refreshKnownUsers() {
        if(knownUsers.value.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                supabaseClient.postgrest.from("profiles").select {
                    User::id isIn knownUsers.value.map(User::id)
                }.decodeAs<List<User>>()
            }.onSuccess {
                println(it)
                knownUsers.value = it
                knownUsersFile.writeText(Json.encodeToString(it))
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun resolveAndAddUser(id: String, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        if(knownUsers.value.any { it.id == id }) {
            pushEvent(UIEvent.AlertEvent("User bereits vorhanden."))
            return
        }
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                supabaseClient.postgrest.from("profiles").select {
                    User::id eq id
                }.decodeAs<List<User>>().firstOrNull() ?: throw IllegalArgumentException("User not found")
            }.onSuccess {
                knownUsers.value = knownUsers.value + it
                knownUsersFile.writeText(Json.encodeToString(knownUsers.value))
                onSuccess()
            //    pushEvent(UIEvent.AlertEvent("User ${it.username} zu den Bekannten Nutzern hinzugefügt."))
            }.onFailure {
                if(it is IllegalArgumentException) {
                  //  pushEvent(UIEvent.AlertEvent("Benutzer nicht gefunden!"))
                    onError()
                } else if(it is RestException) {
                //    pushEvent(UIEvent.AlertEvent("Benutzer nicht gefunden!"))
                    onError()
                }
                it.printStackTrace()
            }
        }
    }

    fun removeKnownUser(id: String) {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                knownUsers.value = knownUsers.value.filter { it.id != id }
                knownUsersFile.writeText(Json.encodeToString(knownUsers.value))
            }
        }
    }

    fun deleteProduct(id: Int) {
        scope.launch {
            kotlin.runCatching {
                productRepository.deleteProduct(id)
            }.onSuccess {
                refreshProducts()
                //     pushEvent(UIEvent.AlertEvent("Produkt wurde gelöscht."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun editProductContent(product: Int, content: String) {
        scope.launch {
            kotlin.runCatching {
                productRepository.editContent(product, content)
            }.onSuccess {
                refreshProducts()
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun createCard(description: String, authorizedUsers: List<String>, file: File) {
        scope.launch {
            kotlin.runCatching {
                val path = cardRepository.uploadImage(file.extension, file.readBytes())
                cardRepository.createCard(description, authorizedUsers, path, supabaseClient.auth.currentSession.value!!.user!!.id)
            }.onSuccess {
                refreshCards()
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun deleteCard(id: Int, imagePath: String) {
        scope.launch {
            kotlin.runCatching {
                cardRepository.deleteCard(id, imagePath)
            }.onSuccess {
                refreshCards()
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun editCard(card: Int, description: String, authorizedUsers: List<String>) {
        scope.launch {
            kotlin.runCatching {
                cardRepository.editCard(card, description, authorizedUsers)
            }.onSuccess {
                refreshCards()
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun refreshCards() {
        scope.launch {
            kotlin.runCatching {
                cardRepository.getCards()
            }.onSuccess {
                cardFlow.value = it.map { card ->
                    if(card.owner.id == supabaseClient.auth.currentSession.value!!.user!!.id) {
                        card.copy(isOwner = true)
                    } else {
                        card
                    }
                }
               // cardCacheManager.saveCache(cardFlow.value, context)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun updateSettings(newSettings: Settings) {
        scope.launch {
            kotlin.runCatching {
                settingsFile.writeText(Json.encodeToString(newSettings))
            }.onSuccess {
                settings.value = newSettings
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Einstellungen konnten nicht aktualisiert werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun readSettings() {
        scope.launch {
            kotlin.runCatching {
                if(!settingsFile.parentFile.exists()) {
                    settingsFile.mkdir()
                    Json.encodeToString(Settings())
                } else if(!settingsFile.exists()) {
                    settingsFile.createNewFile()
                    settingsFile.writeText(Json.encodeToString(Settings()))
                    Json.encodeToString(Settings())
                } else {
                    settingsFile.readText()
                }
            }.onSuccess {
                settings.value = Json.decodeFromString(it)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Einstellungen konnten nicht gelesen werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun pushEvent(event: UIEvent) {
        eventFlow.value = eventFlow.value + event
    }

    fun removeEvent(index: Int) {
        eventFlow.value = eventFlow.value.toMutableList().apply {
            removeAt(index)
        }
    }

}