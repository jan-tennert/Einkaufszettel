package io.github.jan.einkaufszettel.common.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.jan.einkaufszettel.common.repositories.product.Shop

enum class HomeScreenExtraTextMode {
    NO_SHOP,
    NO_PRODUCTS
}

@Composable
fun HomeScreenExtraText(shops: List<Shop>, switchToCreateShop: () -> Unit) {
    val extraText = remember(shops) { if (shops.isEmpty()) HomeScreenExtraTextMode.NO_SHOP else if (shops.none {
            it.products?.isNotEmpty() == true
        }) HomeScreenExtraTextMode.NO_PRODUCTS else null }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when(extraText) {
            HomeScreenExtraTextMode.NO_SHOP -> {
                Column( modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Du bist in keinem Einkaufszettel Mitglied!", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Button(switchToCreateShop) {
                        Text("Einkaufszettel für Shop jetzt erstellen")
                    }
                }
            }
            HomeScreenExtraTextMode.NO_PRODUCTS -> {
                Text("Es gibt bisher keinen Eintrag auf den Einkaufszetteln!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            null -> {
                //ignored
            }
        }
    }
}