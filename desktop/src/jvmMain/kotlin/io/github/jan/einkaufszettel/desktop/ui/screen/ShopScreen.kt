package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.unit.dp
import io.github.jan.einkaufszettel.common.repositories.product.Product
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.ui.dialog.CreateProductDialog
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.components.ProductItemCard
import io.github.jan.supacompose.auth.auth

@Composable
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
//    val showAllUsers by viewModel.showAllUsers.collectAsState()
    val shops by viewModel.shopFlow.collectAsState()
    val shop = shops.first { it.id == id }
    println(shop.products?.size)
    val profile by viewModel.profileFlow.collectAsState()
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                highlightedItem.value = null
            }
        })

    ShopList(shop = shop, viewModel = viewModel)

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
        CreateProductDialog(shop = shop.id, disable = { openCreateScreen = false }, onDone = { viewModel.createProductInShop(shop.id, it, viewModel.supabaseClient.auth.currentSession.value!!.user!!.id) })
    }

    if(highlightedItem.value != null) {
        CreateProductDialog(shop = shop.id, value = highlightedItem.value!!.content, edit = highlightedItem.value, disable = { highlightedItem.value = null }, onDone = { viewModel.editProductContent(highlightedItem.value!!.id, it)})
    }

    /*if(showAllUsers) {
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
                    UserCard(user, viewModel, UserCardMode.Shop)
                }
            }
        }
    }*/
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopList(shop: Shop, viewModel: ProductViewModel) {
    val products = shop.products ?: emptyList()
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(0.9f)
    ) {
        items(products.sortedBy { it.doneBy != null }, { it }) { pr ->
            val item = products.first { it.id == pr.id }
            ProductItemCard(
                item = item,
                modifier = Modifier.animateItemPlacement(),
                viewModel = viewModel
            )
        }
    }
}