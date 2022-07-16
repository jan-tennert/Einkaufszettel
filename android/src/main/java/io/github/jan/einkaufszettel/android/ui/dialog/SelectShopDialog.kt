package io.github.jan.einkaufszettel.android.ui.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.repositories.product.Shop

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectShopDialog(viewModel: ProductViewModel, onDismissRequest: () -> Unit, onShopSelected: (shop: Shop) -> Unit) {
    val shops by viewModel.shopFlow.collectAsState()
    Dialog(onDismissRequest) {
        LazyColumn {
            shops.forEach { shop ->
                item {
                    Card(onClick = {
                        onShopSelected(shop)
                    }, modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(15.dp)) {
                            AsyncImage(
                                shop.iconUrl,
                                "",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                            Text(shop.name, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                }
            }
        }
    }
}