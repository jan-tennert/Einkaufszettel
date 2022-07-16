package io.github.jan.einkaufszettel.desktop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Navigator(defaultRoute: String) {

    private val _currentRoute = MutableStateFlow(defaultRoute)
    val currentRoute = _currentRoute.asStateFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

}

class NavHost internal constructor(private val navigator: Navigator) {

    private val handlers = mutableMapOf<String, @Composable () -> Unit>()

    fun on(route: String, handle: @Composable () -> Unit) {
        handlers[route] = handle
    }

    @Composable
    fun host() {
        val route by navigator.currentRoute.collectAsState()
        handlers[route]?.invoke()
    }

}

@Composable
fun NavHost(navigator: Navigator, routeHandlers: NavHost.() -> Unit) = NavHost(navigator).apply(routeHandlers).host()