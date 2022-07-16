
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.jan.einkaufszettel.common.Constants
import io.github.jan.einkaufszettel.common.modules.cacheModule
import io.github.jan.einkaufszettel.common.modules.networkModule
import io.github.jan.einkaufszettel.common.modules.productInfoModule
import io.github.jan.einkaufszettel.common.modules.productModule
import io.github.jan.einkaufszettel.common.modules.profileModule
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.einkaufszettel.common.ui.dialog.ProfileDialog
import io.github.jan.einkaufszettel.common.ui.screen.AuthScreen
import io.github.jan.einkaufszettel.common.ui.theme.EinkaufszettelTheme
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.ui.dialog.VersionDialog
import io.github.jan.einkaufszettel.desktop.ui.screen.MainScreen
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(productModule, productInfoModule, profileModule, cacheModule, networkModule)
    }
    application {
        Window(onCloseRequest = ::exitApplication, state = rememberWindowState(placement = WindowPlacement.Maximized), title = "Einkaufszettel", icon = painterResource("orders.png")) {
            val viewModel = remember { ProductViewModel() }
            val settings by viewModel.settings.collectAsState()
            EinkaufszettelTheme(settings.darkMode) {
                //App()
                val session by viewModel.supabaseClient.auth.currentSession.collectAsState()
                val status by viewModel.supabaseClient.auth.status.collectAsState()
                val userProfile by viewModel.profileFlow.collectAsState()
                val events by viewModel.eventFlow.collectAsState()
                val version by viewModel.versionFlow.collectAsState()
                println(userProfile)
                if(version <= -1 || version <= Constants.VERSION) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        println(session?.accessToken)
                        if(status == Auth.Status.NOT_AUTHENTICATED) {
                            AuthScreen(viewModel)
                        } else if(userProfile is UserStatus.NotFound && status == Auth.Status.AUTHENTICATED) {
                            session?.let { ProfileDialog(it, { name -> viewModel.createProfile(session!!.user!!.id, name)}, {}) }
                        } else if(status == Auth.Status.LOADING_FROM_STORAGE) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        } else if(status in listOf(Auth.Status.AUTHENTICATED, Auth.Status.NETWORK_ERROR)) {
                            LaunchedEffect(Unit) {
                                addRefreshLoop(viewModel, session)
                            }
                            MainScreen(viewModel)
                        }
                        events.forEachIndexed { index, uiEvent ->
                            uiEvent.Draw {
                                viewModel.removeEvent(index)
                            }
                        }
                    }
                } else if(version != 0 && version > Constants.VERSION) {
                    VersionDialog(newVersion = version) {
                        viewModel.versionFlow.value = -2
                    }
                }
            }
        }
    }
}

private suspend fun CoroutineScope.addRefreshLoop(viewModel: ProductViewModel, session: UserSession?) {
    while(true) {
        outer@ while(isActive) {
            while(isActive && session == null) {
                delay(500)
            }
            viewModel.getLatestVersion()
            viewModel.refreshProducts()
            viewModel.readSettings()
            viewModel.loadProfile(viewModel.supabaseClient.auth.currentSession.value!!.user!!.id, false)
            viewModel.loadKnownUsers()
            viewModel.refreshCards()
            while (isActive) {
                delay(13000)
                if(viewModel.supabaseClient.auth.currentSession.value == null) continue@outer
                viewModel.getLatestVersion()
                viewModel.refreshProducts()
                viewModel.loadProfile(viewModel.supabaseClient.auth.currentSession.value!!.user!!.id, false)
                viewModel.refreshKnownUsers()
                viewModel.refreshCards()
                //   viewModel.refreshAttachments(applicationContext)
                //  viewModel.getLatestVersion()
            }
        }
    }
}