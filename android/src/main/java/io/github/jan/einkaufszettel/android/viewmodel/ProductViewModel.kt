package io.github.jan.einkaufszettel.android.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.einkaufszettel.common.cache.CardCacheManager
import io.github.jan.einkaufszettel.common.cache.ProductCacheManager
import io.github.jan.einkaufszettel.common.cache.loadCache
import io.github.jan.einkaufszettel.common.cache.saveCache
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
import io.github.jan.einkaufszettel.common.repositories.shop.uploadIcon
import io.github.jan.einkaufszettel.common.repositories.uploadImage
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.providers.Email
import io.github.jan.supacompose.auth.providers.Google
import io.github.jan.supacompose.exceptions.RestException
import io.github.jan.supacompose.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.UnknownHostException

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val profileRepository: ProfileRepository,
    val supabaseClient: SupabaseClient,
    val shopRepository: ShopRepository,
    val cardRepository: CardRepository,
    val cardCacheManager: CardCacheManager,
    val productCacheManager: ProductCacheManager,
    private val httpClient: HttpClient
) : ViewModel(), AuthController {

    val shopFlow = MutableStateFlow<List<Shop>>(emptyList())
    val areShopRefreshing = MutableStateFlow(false)
    val cardFlow = MutableStateFlow<List<Card>>(emptyList())
    val eventFlow = MutableStateFlow<List<UIEvent>>(emptyList())
    val versionFlow = MutableStateFlow(-1)
    //val lastRefreshFlow = MutableStateFlow<DateTimeTz?>(null)
    val connectionStatus = MutableStateFlow(false)
    val orderFlow = mutableStateMapOf<Int, List<Int>>()
    val profileFlow = MutableStateFlow<UserStatus>(UserStatus.NotTried)
    val knownUsers = MutableStateFlow(emptyList<User>())
    val showAllUsers = MutableStateFlow(false)
    val editShopOrder = MutableStateFlow(false)

    override fun signUpWithEmail(email: String, password: String, callback: () -> Unit) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.loginWith(Google)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun sendPasswordRecovery(email: String) {
        viewModelScope.launch {
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

    fun loadKnownUsers(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "knownUsers.json")
            if(!file.exists()) {
                file.createNewFile()
                file.writeText("[]")
                return@launch
            } else {
                knownUsers.value = Json.decodeFromString(file.readText())
            }
        }
    }

    fun refreshKnownUsers(context: Context) {
        if(knownUsers.value.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                supabaseClient.postgrest.from("profiles").select {
                    User::id isIn knownUsers.value.map(User::id)
                }.decodeAs<List<User>>()
            }.onSuccess {
                println(it)
                knownUsers.value = it
                File(context.filesDir, "knownUsers.json").writeText(Json.encodeToString(it))
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun resolveAndAddUser(id: String, context: Context) {
        if(knownUsers.value.any { it.id == id }) {
            pushEvent(UIEvent.AlertEvent("User bereits vorhanden."))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                supabaseClient.postgrest.from("profiles").select {
                    User::id eq id
                }.decodeAs<List<User>>().firstOrNull() ?: throw IllegalArgumentException("User not found")
            }.onSuccess {
                knownUsers.value = knownUsers.value + it
                File(context.filesDir, "knownUsers.json").writeText(Json.encodeToString(knownUsers.value))
                pushEvent(UIEvent.AlertEvent("User ${it.username} zu den Bekannten Nutzern hinzugefügt."))
            }.onFailure {
                if(it is IllegalArgumentException) {
                    pushEvent(UIEvent.AlertEvent("Benutzer nicht gefunden!"))
                }
                it.printStackTrace()
            }
        }
    }

    fun removeKnownUser(id: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                knownUsers.value = knownUsers.value.filter { it.id != id }
                File(context.filesDir, "knownUsers.json").writeText(Json.encodeToString(knownUsers.value))
            }
        }
    }

    fun updateUsername(id: String, name: String, callback: () -> Unit) {
        viewModelScope.launch {
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

    fun changePasswordTo(password: String) {
        viewModelScope.launch {
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

    fun logout(context: Context) {
        viewModelScope.launch {
            kotlin.runCatching {
                supabaseClient.auth.invalidateSession()
                profileFlow.value = UserStatus.NotFound
                File(context.filesDir, "profile.json")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun loadProfile(id: String, first: Boolean) {
        if(first) {
            profileFlow.value = UserStatus.Loading
        }
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch {
            kotlin.runCatching {
                profileRepository.createProfile(id, username)
            }.onSuccess {
                profileFlow.value = UserStatus.Success(it)
            }.onFailure {
                it.printStackTrace()
                when(it) {
                    is RestException -> {
                        if(it.status == 409) {
                            pushEvent(UIEvent.AlertEvent("Der Benutzer ist bereits vorhanden. Bitte wähle einen anderen Namen"))
                        } else {
                            pushEvent(UIEvent.AlertEvent("Fehler beim Erstellen des Profils. Bitte überprüfe deine Internetverbindung."))
                        }
                    }
                    else -> pushEvent(UIEvent.AlertEvent("Fehler beim Erstellen des Profils. Bitte überprüfe deine Internetverbindung."))
                }
                profileFlow.value = UserStatus.NotFound
            }
        }
    }

    fun refreshProducts(context: Context, callback: () -> Unit = {}) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.getShops()
            }.onSuccess {
                shopFlow.value = it
                //lastRefreshFlow.value = DateTime.nowLocal()
                updateOrder(context)
                connectionStatus.value = true
                productCacheManager.saveCache(shopFlow.value, context)
                callback()
            }.onFailure {
                it.printStackTrace()
                connectionStatus.value = false
            }
        }
    }

    @SuppressLint("NewApi")
    fun createProductInShop(context: Context, shop: Int, content: String, creatorId: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.createInShop(shop, content, creatorId)
            }.onSuccess {
                refreshProducts(context)
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun markProductAsDone(context: Context, product: Int, userId: String, callback: () -> Unit) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.markAsDone(product, userId)
            }.onSuccess {
                refreshProducts(context, callback)
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                callback()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht als erledigt markiert werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun markProductAsNotDone(context: Context, product: Int, callback: () -> Unit) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.markAsNotDone(product)
            }.onSuccess {
                refreshProducts(context, callback)
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                callback()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht als erledigt markiert werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun deleteProduct(context: Context, id: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.deleteProduct(id)
            }.onSuccess {
                refreshProducts(context)
           //     pushEvent(UIEvent.AlertEvent("Produkt wurde gelöscht."))
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun editProductContent(context: Context, product: Int, content: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                productRepository.editContent(product, content)
            }.onSuccess {
                refreshProducts(context)
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Produkt konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun createCard(context: Context, description: String, authorizedUsers: List<String>, imageUri: Uri) {
        viewModelScope.launch {
            kotlin.runCatching {
                val path = cardRepository.uploadImage(context, imageUri)
                cardRepository.createCard(description, authorizedUsers, path, supabaseClient.auth.currentSession.value!!.user!!.id)
            }.onSuccess {
                refreshCards(context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun deleteCard(context: Context, id: Int, imagePath: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                cardRepository.deleteCard(id, imagePath)
            }.onSuccess {
                refreshCards(context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun editCard(context: Context, card: Int, description: String, authorizedUsers: List<String>) {
        viewModelScope.launch {
            kotlin.runCatching {
                cardRepository.editCard(card, description, authorizedUsers)
            }.onSuccess {
                refreshCards(context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Karte konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
        }
    }

    fun refreshCards(context: Context) {
        viewModelScope.launch {
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
                cardCacheManager.saveCache(cardFlow.value, context)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun addCardCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            cardFlow.value = cardCacheManager.loadCache(context)
        }
    }

    fun createShop(
        context: Context,
        name: String,
        icon: Uri,
        authorizedUsers: List<String>,
        callback: () -> Unit
    ) {
        viewModelScope.launch {
            kotlin.runCatching {
                val key = shopRepository.uploadIcon(context, icon)
                shopRepository.createShop(name, supabaseClient.path("storage/v1/object/public/$key"), supabaseClient.auth.currentSession.value!!.user!!.id, authorizedUsers)
            }.onSuccess {
                refreshProducts(context)
                pushEvent(UIEvent.AlertEvent("Shop wurde erstellt."))
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht erstellt werden. Überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    fun editShop(context: Context, shop: Int, newName: String, authorizedUsers: List<String>, callback: () -> Unit) {
        viewModelScope.launch {
            kotlin.runCatching {
                shopRepository.editShop(shop, newName, authorizedUsers)
            }.onSuccess {
                refreshProducts(context)
                pushEvent(UIEvent.AlertEvent("Shop wurde bearbeitet."))
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht bearbeitet werden. Überprüfe deine Internetverbindung."))
            }
            callback()
        }
    }

    fun deleteShop(context: Context, id: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                shopRepository.deleteShop(id)
            }.onSuccess {
                refreshProducts(context)
                pushEvent(UIEvent.AlertEvent("Shop wurde gelöscht."))
                productCacheManager.saveCache(shopFlow.value, context)
            }.onFailure {
                it.printStackTrace()
                pushEvent(UIEvent.AlertEvent("Shop konnte nicht gelöscht werden. Überprüfe deine Internetverbindung."))
            }
        }
    }


    fun getLatestVersion() {
        viewModelScope.launch {
            kotlin.runCatching {
                httpClient.get("http://xowugqynwpspvhno.myfritz.net:26666/api/shopping/version").body<JsonObject>()["version"]!!.jsonPrimitive.int
            }.onSuccess {
                println(it)
                versionFlow.value = it
            }.onFailure {
                versionFlow.value = -1
            }
        }
    }

    fun clearCache(context: Context): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.deleteRecursively()
         //   imageFlow.value = listOf()
            shopFlow.value = listOf()
            File(context.filesDir, "profile.json").delete()
            clearOrder(context)
        }
    }

    private fun clearOrder(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            File(context.filesDir, "order.json").delete()
            File(context.dataDir, "order.json").delete()
            orderFlow.clear()
        }
    }

    fun addProductCache(context: Context) {
        viewModelScope.launch {
            shopFlow.value = productCacheManager.loadCache(context)
        }
    }

    @SuppressLint("NewApi")
    private fun updateOrder(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val orderFile = File(context.filesDir, "order.json")
            val currentOrder = orderFlow.toMap()
            shopFlow.value.forEach { shop ->
                val missingItems = (shop.products ?: emptyList()).map { it.id } - currentOrder.getOrDefault(shop.id, emptyList()).toSet()
                val deletedItems = currentOrder.getOrDefault(shop.id, emptyList()) - (shop.products ?: emptyList()).map { it.id }.toSet()
                orderFlow[shop.id] = orderFlow.getOrDefault(shop.id, listOf()).filter { it !in deletedItems }.plus(missingItems)
            }
            orderFlow.filter { (id, _) ->
                id !in shopFlow.value.map(Shop::id)
            }.forEach { (t, _) ->
                orderFlow.remove(t)
            }
            if(!orderFile.exists()) orderFile.createNewFile().also {
                orderFile.writeText("""
                {
                }
            """.trimIndent())
            }
            orderFile.writeText(Json.encodeToString(orderFlow.toMap()))
        }
    }

    @SuppressLint("NewApi")
    fun moveId(shop: Int, from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentOrder = orderFlow.getOrDefault(shop, emptyList())
            orderFlow[shop] = currentOrder.toMutableList().apply {
                add(from, removeAt(to))
            }
        }
    }

    fun saveOrder(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val orderFile = File(context.filesDir, "order.json")
            if(!orderFile.exists()) orderFile.createNewFile()
            orderFile.writeText(Json.encodeToString(orderFlow.toMap()))
        }
    }

    @SuppressLint("NewApi")
    fun loadOrder(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val orderFile = File(context.filesDir, "order.json")
            if(!orderFile.exists()) orderFile.createNewFile().also {
                orderFile.writeText("""
                {
                }
            """.trimIndent())
            } else {
                orderFlow.clear()
                orderFlow.putAll(Json.decodeFromString(orderFile.readText()))
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