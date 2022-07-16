package io.github.jan.einkaufszettel.android.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.android.components.ProductItemCard
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.components.UserCard
import io.github.jan.einkaufszettel.common.components.UserCardMode
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.einkaufszettel.common.repositories.product.Product
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.repositories.product.User
import io.github.jan.einkaufszettel.common.ui.dialog.CreateProductDialog
import io.github.jan.supacompose.auth.auth
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@SuppressLint("NewApi")
@Composable
@Destination
fun ShopScreen(
    id: Int,
    viewModel: ProductViewModel
) {
    var openCreateScreen by remember {
        mutableStateOf(false)
    }
    val highlightedItem = remember {
        mutableStateOf<Product?>(null)
    }
    val showAllUsers by viewModel.showAllUsers.collectAsState()
    val editShopOrder by viewModel.editShopOrder.collectAsState()
    val shops by viewModel.shopFlow.collectAsState()
    val shop = shops.first { it.id == id }
    val profile by viewModel.profileFlow.collectAsState()
    val areShopsRefreshing by viewModel.areShopRefreshing.collectAsState()
    val context = LocalContext.current
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                highlightedItem.value = null
            }
        })

    SwipeRefresh(rememberSwipeRefreshState(areShopsRefreshing), {
        viewModel.areShopRefreshing.value = true
        viewModel.refreshProducts(context) {
            viewModel.areShopRefreshing.value = false
        }
    }) {
        ReorderableList(shop = shop, viewModel = viewModel, editShopOrder = editShopOrder)
    }

    if(!openCreateScreen && highlightedItem.value == null) {
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(onClick = {
                openCreateScreen = true
            }, modifier = Modifier.offset(x = (-10).dp, y = (-10).dp)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }

    if(openCreateScreen) {
        CreateProductDialog(shop = shop.id, disable = { openCreateScreen = false }, onDone = { viewModel.createProductInShop(context, shop.id, it, viewModel.supabaseClient.auth.currentSession.value!!.user!!.id) })
    }

    if(highlightedItem.value != null) {
        CreateProductDialog(shop = shop.id, value = highlightedItem.value!!.content, edit = highlightedItem.value, disable = { highlightedItem.value = null }, onDone = { viewModel.editProductContent(context, highlightedItem.value!!.id, it) })
    }

    if(showAllUsers) {
        Dialog(onDismissRequest = { viewModel.showAllUsers.value = false }) {
            LazyColumn {
                val users = (shop.authorizedUsers ?: emptyList()).let {
                    if(profile is UserStatus.Success && (profile as UserStatus.Success).profile.id !in it.map(User::id) && (profile as UserStatus.Success).profile.id == shop.ownerId) {
                        it.plus(User(shop.ownerId, shop.ownerName))
                    } else {
                        it
                    }
                }
                items(users.size, { users[it].id }) {
                    val user = users[it]
                    UserCard(user, UserCardMode.Shop, { id -> viewModel.removeKnownUser(id, context)}, { id -> viewModel.resolveAndAddUser(id, context)}, { id -> setClipboard(context, id) })
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ReorderableList(shop: Shop, viewModel: ProductViewModel, editShopOrder: Boolean) {
    val state = rememberReorderableLazyListState({ from, to ->
        viewModel.moveId(shop.id, from.index, to.index)
    })
    val productOrder = viewModel.orderFlow
    val products = shop.products ?: emptyList()
    val shopProducts = productOrder.getOrDefault(shop.id, emptyList())
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .reorderable(state)
    ) {
        items(shopProducts.sortedBy {
            val item = products.first { pr -> pr.id == it }
            item.doneBy != null
        }, { it }) { id ->
            val item = products.first { it.id == id }
            ReorderableItem(state, id) {
                ProductItemCard(
                    isDragging = it,
                    item = item,
                    reorderable = state,
                    showMoveButton = products.size > 1 && editShopOrder,
                    productViewModel = viewModel
                )
            }
        }
    }
}