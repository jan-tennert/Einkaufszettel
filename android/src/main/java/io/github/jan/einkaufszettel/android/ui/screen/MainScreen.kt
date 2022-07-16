package io.github.jan.einkaufszettel.android.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import io.github.jan.einkaufszettel.android.navigateTo
import io.github.jan.einkaufszettel.android.ui.screen.destinations.AccountScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.BarCodeScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.CardsScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.HomeScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.KnownUsersScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.NewScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.SettingsScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.ShopCreateScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.ShopEditScreenDestination
import io.github.jan.einkaufszettel.android.ui.screen.destinations.ShopScreenDestination
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.icons.Group
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.WifiOff
import io.github.jan.einkaufszettel.common.navigation.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(
    viewModel: ProductViewModel = viewModel()
) {

    val scaffoldState = rememberScaffoldState(rememberDrawerState(initialValue = DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val engine = rememberAnimatedNavHostEngine()
    val navController = engine.rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomMenuItems = remember { MenuItem.Bottom.all() }
    val connected by viewModel.connectionStatus.collectAsState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(
                scope = scope,
                scaffoldState = scaffoldState,
                currentRoute = currentRoute,
                connected = connected,
                viewModel = viewModel
            )
        },
        drawerContent = {
            Drawer(scope, scaffoldState, navController, bottomMenuItems, currentRoute)
        }
    ) {
        DestinationsNavHost(navGraph = NavGraphs.root, engine = engine, navController = navController, dependenciesContainerBuilder = {
            dependency(viewModel)
        })
    }

}

@Composable
fun TopBar(scope: CoroutineScope, scaffoldState: ScaffoldState, currentRoute: String?, connected: Boolean, viewModel: ProductViewModel) {
    TopAppBar(
        title = {
            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                Row {
                    if(!connected) {
                        Icon(MIcon.WifiOff, "Not connected to internet", tint = Color.Red, modifier = Modifier.padding(end = 12.dp))
                    }
                    if(currentRoute != null && currentRoute == "shop_screen/{id}") {
                        Row {
                            val editShopOrder by viewModel.editShopOrder.collectAsState()
                            val context = LocalContext.current
                            IconButton({
                                viewModel.showAllUsers.value = true
                            }, modifier = Modifier.padding(end = 6.dp)) {
                                Icon(MIcon.Group, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                            }
                            if(editShopOrder) {
                                IconButton({
                                    viewModel.editShopOrder.value = false
                                    viewModel.saveOrder(context)
                                }, modifier = Modifier.padding(end = 10.dp)) {
                                    Icon(Icons.Filled.Done, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                                }
                            } else {
                                IconButton({
                                    viewModel.editShopOrder.value = true
                                }, modifier = Modifier.padding(end = 10.dp)) {
                                    Icon(Icons.Filled.Edit, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                                }
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun Drawer(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    navController: NavController,
    bottomItems: List<MenuItem.Bottom>,
    currentRoute: String?,
) {
    val viewModel = getViewModel<ProductViewModel>()
    val shops by viewModel.shopFlow.collectAsState()

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
                        navController.navigateTo(HomeScreenDestination, scaffoldState, scope)
                    })
                }
            }
            items(sortedShops, { it.id }) {
                DrawerItem(it.name, {
                    it.Image(Modifier.size(40.dp))
                }, selected = currentRoute == it.id.toString(), onLong = {
                    navController.navigateTo(ShopEditScreenDestination(it.id), scaffoldState, scope)
                }, shopAmount = it.products?.filter { pr -> pr.doneBy == null }?.size ?: 0) {
                    viewModel.editShopOrder.value = false
                    navController.navigateTo(ShopScreenDestination(it.id), scaffoldState, scope)
                }
            }
        }

        Box(contentAlignment = Alignment.BottomStart) {
            Column {
                bottomItems.forEach { item ->
                    DrawerItem(title = item.title, icon = { item.Icon() }, selected = currentRoute == item.route, onItemClick = {
                        val destination = when(item) {
                            MenuItem.Bottom.Account -> AccountScreenDestination
                            MenuItem.Bottom.BarCodeScanner -> BarCodeScreenDestination
                            MenuItem.Bottom.Cards -> CardsScreenDestination
                            MenuItem.Bottom.Changelog -> NewScreenDestination
                            MenuItem.Bottom.CreateShop -> ShopCreateScreenDestination
                            MenuItem.Bottom.KnownUsers -> KnownUsersScreenDestination
                            MenuItem.Bottom.Settings -> SettingsScreenDestination
                        }
                        navController.navigateTo(destination, scaffoldState, scope)
                    })
                }
            }
        }

    }
    /*Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.fillMaxSize()) {
        Text(buildAnnotatedString {
            append("Version: ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(Constants.VERSION.toString())
            }
            append(" Ersteller: ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Jan")
            }
        }, modifier = Modifier.offset(x = 16.dp))
    }*/

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