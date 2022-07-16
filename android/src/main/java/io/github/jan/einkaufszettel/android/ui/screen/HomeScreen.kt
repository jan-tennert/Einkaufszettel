package io.github.jan.einkaufszettel.android.ui.screen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import io.github.jan.einkaufszettel.android.components.ProductItemCard
import io.github.jan.einkaufszettel.android.ui.screen.destinations.ShopCreateScreenDestination
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.ui.screen.HomeScreenExtraText
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen(
    viewModel: ProductViewModel,
    navController: NavController
) {
    val shops by viewModel.shopFlow.collectAsState()
  /*  val highlightedItem = remember {
        mutableStateOf<ProductItem?>(null)
    }*/
    val areShopsRefreshing by viewModel.areShopRefreshing.collectAsState()
    val context = LocalContext.current

    SwipeRefresh(rememberSwipeRefreshState(areShopsRefreshing), {
        viewModel.areShopRefreshing.value = true
        viewModel.refreshProducts(context) {
            viewModel.areShopRefreshing.value = false
        }
    }) {
        ReorderableAllList(shops, viewModel, navController)
    }

  /*  if(highlightedItem.value != null) {
        val shop = products.toList().find { (_, products) -> highlightedItem.value in products }?.first ?: throw IllegalStateException("Product not found")
        CreateScreen(shop, username, highlightedItem.value, disable = { highlightedItem.value = null }, viewModel = viewModel)
    }*/
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableAllList(shops: List<Shop>, viewModel: ProductViewModel, navController: NavController) {
    val state = rememberReorderableLazyListState({ _, _ ->})
    val productOrders = viewModel.orderFlow
    val foldMap = mutableStateMapOf<Int, Boolean>()

    LazyColumn(
        state = state.listState,
        modifier = Modifier.fillMaxHeight(0.9f),
    ) {
        productOrders.filter { it.value.isNotEmpty() }.forEach { (shopId, ids) ->
            val shop = shops.firstOrNull { it.id == shopId } ?: return@forEach
            item {
                Box(modifier = Modifier.fillMaxWidth().clickable {
                    foldMap[shop.id] = !(foldMap[shop.id] ?: false)
                }.animateItemPlacement(), contentAlignment = Alignment.Center) {
                    Text(shop.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            if(foldMap[shop.id] != true) {
                items(ids.sortedBy {
                    val item = (shop.products ?: emptyList()).first { pr -> pr.id == it }
                    item.doneBy != null
                }, { it }) { id ->
                    val item = (shop.products ?: emptyList()).first { it.id == id }
                    //val index = ids.indexOf(id)
                    ProductItemCard(
                        reorderable = state,
                        item = item,
                        showMoveButton = false,
                        productViewModel = viewModel,
                        modifier = Modifier.animateItemPlacement(),
                        isDragging = false
                    )
                }
            }
        }
    }

    HomeScreenExtraText(shops) {
        navController.navigate(ShopCreateScreenDestination)
    }
}
