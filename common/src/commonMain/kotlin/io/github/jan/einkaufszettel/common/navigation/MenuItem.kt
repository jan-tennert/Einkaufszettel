package io.github.jan.einkaufszettel.common.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.jan.einkaufszettel.common.icons.CreditCard
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.People
import io.github.jan.einkaufszettel.common.icons.PhotoCamera

sealed interface MenuItem {

    val title: String
    val iconVector: ImageVector
    val route: String

    @Composable
    fun Icon() {
        Image(
            iconVector,
            "",
            colorFilter = ColorFilter.tint(if (!MaterialTheme.colors.isLight) Color.White else Color.Black),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(24.dp)
                .width(24.dp)
        )
    }

    sealed class Bottom(
        override val title: String,
        override val iconVector: ImageVector,
        override val route: String
    ) : MenuItem {

        object CreateShop : Bottom("Shop erstellen", Icons.Filled.AddCircle, "create_shop")
        object BarCodeScanner : Bottom("Barcode scannen", MIcon.PhotoCamera, "bar_code_scanner")
        object Cards : Bottom("Karten", MIcon.CreditCard, "cards")
        object KnownUsers : Bottom("Bekannte Nutzer", MIcon.People, "known_users")
        object Account : Bottom("Account", Icons.Filled.AccountCircle, "account")
        object Changelog : Bottom("Changelog", Icons.Filled.Info, "changelog")
        object Settings : Bottom("Einstellungen", Icons.Filled.Settings, "settings")

        companion object {

            fun all() = Bottom::class.sealedSubclasses.mapNotNull { it.objectInstance }

        }
    }

    sealed class Independent(
        override val title: String,
        override val iconVector: ImageVector,
        override val route: String
    ) : MenuItem {

        object Home : Independent("Home", Icons.Filled.Home, "home")

    }

}

/*sealed class NavigationItem private constructor(val route: String, val title: String, val isNormal: Boolean, val paintIcon: @Composable (isTitle: Boolean) -> Unit) {

    object EditShop : NavigationItem("editShop", "Shop bearbeiten", Icons.Filled.Edit, false)
    object CreateShop : NavigationItem("shop_creation", "Shop erstellen", Icons.Filled.AddCircle, true)
    object BarCode : NavigationItem("barcode", "Barcode scannen", Icons.Filled.PhotoCamera, true)
    object Cards : NavigationItem("cards", "Karten", Icons.Filled.CreditCard, true)
    object KnownUsers : NavigationItem("knownUsers", "Bekannte Nutzer", Icons.Filled.People, true)
    object Account : NavigationItem("account", "Account", Icons.Filled.AccountCircle, true)
    object New : NavigationItem("new", "Changelog", Icons.Filled.Info, true)
    object Shop : NavigationItem("shop", "Shop", Icons.Filled.Store, false)
    object Settings : NavigationItem("settings", "Einstellungen", Icons.Filled.Settings, true)
    //object Alle : NavigationItem("alle", "Alle", Icons.Filled.Menu, false)
    //object Misc : NavigationItem("general", "Allgemein", Icons.Filled.Assessment)
    //object Shop : NavigationItem("shop", "Shop", Icons.Filled.Store, false)
    object Home : NavigationItem("home", "Home", Icons.Filled.Home, false)

}*/