package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.einkaufszettel.common.icons.Group
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.navigation.MenuItem
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.ui.screen.NewScreen
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.navigation.NavHost
import io.github.jan.einkaufszettel.desktop.navigation.Navigator
import io.github.jan.einkaufszettel.desktop.ui.shape.DrawerShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: ProductViewModel) {
    val scaffoldState = rememberScaffoldState()
    val navigator = remember { Navigator(MenuItem.Independent.Home.route) }
    val currentRoute by navigator.currentRoute.collectAsState()
    val scope = rememberCoroutineScope()
    val shops by viewModel.shopFlow.collectAsState()
    val selectedShop = remember { mutableStateOf<Shop?>(null) }
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Drawer(viewModel, currentRoute, navigator, scaffoldState, selectedShop)
        },
        topBar = {
            TopBar(scope, scaffoldState, currentRoute)
        },
        drawerShape = DrawerShape
    ) {
        NavHost(navigator) {
            println(shops.size)
            shops.forEach {
                on(it.id.toString()) {
                    ShopScreen(it.id, viewModel)
                }
            }
            on(MenuItem.Independent.Home.route) {
                HomeScreen(viewModel, navigator)
            }
            on(MenuItem.Bottom.Settings.route) {
                SettingsScreen(viewModel)
            }
            on(MenuItem.Bottom.Cards.route) {
                CardsScreen(viewModel)
            }
            on(MenuItem.Bottom.KnownUsers.route) {
                KnownUsersScreen(viewModel)
            }
            on(MenuItem.Bottom.Account.route) {
                AccountScreen(viewModel)
            }
            on(MenuItem.Bottom.CreateShop.route) {
                ShopCreateScreen(viewModel)
            }
            on("shop_edit") {
                ShopEditScreen(selectedShop.value!!.id, viewModel, navigator)
            }
            on(MenuItem.Bottom.Changelog.route) {
                NewScreen()
            }
        }
    }
}

@Composable
fun TopBar(scope: CoroutineScope, scaffoldState: ScaffoldState, currentRoute: String?) {
    TopAppBar(
        title = {
            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                Row {
                    if(currentRoute != null && currentRoute == "shop_screen/{id}") {
                        Row {
                            IconButton({
                            //    viewModel.showAllUsers.value = true
                            }, modifier = Modifier.padding(end = 6.dp)) {
                                Icon(MIcon.Group, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(Icons.Filled.Menu, "")
            }
        },
    )
}

@Composable
fun Drawer(viewModel: ProductViewModel, currentRoute: String, navigator: Navigator, scaffoldState: ScaffoldState, selectedShop: MutableState<Shop?>) {
    val shops by viewModel.shopFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomMenuItems = remember { MenuItem.Bottom.all().minus(listOf(MenuItem.Bottom.BarCodeScanner).toSet()) }
    Column(verticalArrangement= Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
        val sortedShops = remember(shops) { shops.sortedBy { it.id } }
        LazyColumn(
            modifier = Modifier
                //  .verticalScroll(rememberScrollState())
                .weight(1f, false)

        ) {
            item {
                Box {
                    DrawerItem(title = MenuItem.Independent.Home.title, icon = { MenuItem.Independent.Home.Icon() }, selected = currentRoute == MenuItem.Independent.Home.route, onItemClick = {
                        navigator.navigateTo(MenuItem.Independent.Home.route, scope, scaffoldState)
                    })
                }
            }
            items(sortedShops, { it.id }) {
                DrawerItem(it.name, {
                    it.Image(Modifier.size(40.dp))
                }, selected = currentRoute == it.id.toString(), onLong = {
                    selectedShop.value = it
                    navigator.navigateTo("shop_edit", scope, scaffoldState)
                }, shopAmount = it.products?.filter { pr -> pr.doneBy == null }?.size ?: 0) {
                 //   viewModel.editShopOrder.value = false
                    navigator.navigateTo(it.id.toString(), scope, scaffoldState)
                }
            }
        }

        Box(contentAlignment = Alignment.BottomStart) {
            Column {
                bottomMenuItems.forEach { item ->
                    DrawerItem(title = item.title, icon = { item.Icon() }, selected = currentRoute == item.route, onItemClick = {
                        navigator.navigateTo(item.route, scope, scaffoldState)
                        //navController.navigateTo(destination, scaffoldState, scope)
                    })
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerItem(title: String, icon: @Composable () -> Unit, selected: Boolean, shopAmount: Int = 0, onLong: () -> Unit = {}, onItemClick: () -> Unit) {
    val background = if (selected) Color(255, 69, 56) else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = {
                onLong()
            }) { onItemClick() }
            .height(45.dp)
            .background(background)
            .padding(start = 10.dp)
    ) {

        icon()

        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = title,
            fontSize = 16.sp,
        )

        if(shopAmount != 0) {
            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                val isLight = MaterialTheme.colors.isLight
                Text(text = shopAmount.toString(), modifier = Modifier.padding(end = 17.dp))
            }
        }
    }

}

fun Navigator.navigateTo(route: String, scope: CoroutineScope, scaffoldState: ScaffoldState) {
    navigateTo(route)

    scope.launch {
        scaffoldState.drawerState.close()
    }
}