package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.jan.einkaufszettel.common.navigation.MenuItem
import io.github.jan.einkaufszettel.common.ui.screen.HomeScreenExtraText
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.components.ProductItemCard
import io.github.jan.einkaufszettel.desktop.navigation.Navigator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ProductViewModel,
    navigator: Navigator
) {
    val shops by viewModel.shopFlow.collectAsState()
    val foldMap = mutableStateMapOf<Int, Boolean>()
    LazyColumn(
        modifier = Modifier.fillMaxHeight(0.9f),
    ) {
        shops.filter { (it.products ?: emptyList()).isNotEmpty() }.forEach { shop ->
            item(shop.id.toString() + shop.name) {
                Box(modifier = Modifier.fillMaxWidth().clickable {
                    foldMap[shop.id] = !(foldMap[shop.id] ?: false)
                }.animateItemPlacement(), contentAlignment = Alignment.Center) {
                    Text(shop.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            if(foldMap[shop.id] != true) {
                items(shop.products?.sortedBy { it.doneBy != null } ?: emptyList(), { it.id }) { product ->
                    //val index = ids.indexOf(id)
                    ProductItemCard(
                        item = product,
                        modifier = Modifier.animateItemPlacement(),
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
    HomeScreenExtraText(shops) {
        navigator.navigateTo(MenuItem.Bottom.CreateShop.route)
    }
}