package io.github.jan.einkaufszettel.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import io.github.jan.einkaufszettel.android.ui.dialog.VersionDialog
import io.github.jan.einkaufszettel.android.ui.screen.MainScreen
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.Constants
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.einkaufszettel.common.ui.dialog.ProfileDialog
import io.github.jan.einkaufszettel.common.ui.screen.AuthScreen
import io.github.jan.einkaufszettel.common.ui.theme.EinkaufszettelTheme
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.Auth
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.initializeAndroid
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {

    private val supabaseClient : SupabaseClient by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAndroid(supabaseClient)
        setContent {
           // val isDarkMode = remember { true }
            val viewModel = getViewModel<ProductViewModel>()
            val version by viewModel.versionFlow.collectAsState()
            val events by viewModel.eventFlow.collectAsState()
            val session by supabaseClient.auth.currentSession.collectAsState()
            val status by supabaseClient.auth.status.collectAsState()
            val userProfile by viewModel.profileFlow.collectAsState()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                addLifecycleCallback(viewModel, supabaseClient.auth.currentSession)
            }
            EinkaufszettelTheme(isSystemInDarkTheme()) {
                println(version)
                if(version == -1 || version <= Constants.VERSION) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        println(session?.accessToken)
                        if(status == Auth.Status.NOT_AUTHENTICATED) {
                            AuthScreen(viewModel)
                        } else if(userProfile is UserStatus.NotFound && status == Auth.Status.AUTHENTICATED) {
                            session?.let { ProfileDialog(it, { name -> viewModel.createProfile(session!!.user!!.id, name)}, { viewModel.logout(context)}, {}) }
                        } else if(status == Auth.Status.LOADING_FROM_STORAGE) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        } else if(status in listOf(Auth.Status.AUTHENTICATED, Auth.Status.NETWORK_ERROR)) {
                            MainScreen(viewModel)
                        }
                    }
                    events.forEachIndexed { index, uiEvent ->
                        uiEvent.Draw {
                            viewModel.removeEvent(index)
                        }
                    }
                } else if(version != 0 && version > Constants.VERSION) {
                    VersionDialog(newVersion = version)
                }
            }
        }
    }

    private fun addLifecycleCallback(
        viewModel: ProductViewModel,
        currentSession: StateFlow<UserSession?>
    ) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {

                private var job: Job? = null

                override fun onStart(owner: LifecycleOwner) {
                    // Application goes to foreground, setup job
                    job = lifecycle.coroutineScope.launch {
                      //  viewModel.addCacheAttachments(applicationContext)
                        viewModel.addProductCache(applicationContext)
                        viewModel.addCardCache(applicationContext)
                        viewModel.loadOrder(applicationContext)
                        viewModel.loadKnownUsers(applicationContext)
                        viewModel.getLatestVersion()
                        outer@ while(isActive) {
                            while(isActive && currentSession.value == null) {
                                delay(500)
                            }
                            viewModel.loadProfile(currentSession.value!!.user!!.id, true)
                            viewModel.refreshProducts(applicationContext)
                            viewModel.refreshKnownUsers(applicationContext)
                            viewModel.refreshCards(applicationContext)
                            // viewModel.refreshAttachments(applicationContext)
                            while (isActive) {
                                delay(13000)
                                if(viewModel.supabaseClient.auth.currentSession.value == null) continue@outer
                                viewModel.refreshProducts(applicationContext)
                                viewModel.loadProfile(currentSession.value!!.user!!.id, false)
                                viewModel.refreshKnownUsers(applicationContext)
                                viewModel.refreshCards(applicationContext)
                                //   viewModel.refreshAttachments(applicationContext)
                                 viewModel.getLatestVersion()
                            }
                        }
                    }
                }
                override fun onStop(owner: LifecycleOwner) {
                    // Application goes to background, cancel job
                    println("stop")
                    job?.cancel()
                }
            }
        )
    }
}