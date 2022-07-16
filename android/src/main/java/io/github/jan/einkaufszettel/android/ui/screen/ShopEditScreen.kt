package io.github.jan.einkaufszettel.android.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.jan.einkaufszettel.android.ui.screen.destinations.HomeScreenDestination
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.repositories.product.User
import io.github.jan.supacompose.auth.auth

@Composable
@Destination
fun ShopEditScreen(id: Int, viewModel: ProductViewModel, navigator: DestinationsNavigator) {
    val shops by viewModel.shopFlow.collectAsState()
    val shop = shops.first { it.id == id }
    var name by remember { mutableStateOf(shop.name) }
    val context = LocalContext.current
    var editing by rememberSaveable { mutableStateOf(false) }
    val knownUsers by viewModel.knownUsers.collectAsState()
    val user by viewModel.supabaseClient.auth.currentSession.collectAsState()
    val isOwner = user?.user?.id == shop.ownerId
    if(isOwner) {
        if (!editing) {
            val selectedUsers = remember {
                mutableStateListOf<User>().apply {
                    addAll(shop.authorizedUsers ?: emptyList())
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column {
                    var expanded by remember { mutableStateOf(false) }
                    TextField(name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .align(Alignment.CenterHorizontally),
                        singleLine = true
                    )
                    val accessToken =
                        viewModel.supabaseClient.auth.currentSession.value!!.accessToken
                    shop.Image(Modifier.size(100.dp).border(2.dp, if (MaterialTheme.colors.isLight) Color.Black else Color.White).align(Alignment.CenterHorizontally))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box {
                            Button({
                                expanded = !expanded
                            }, modifier = Modifier) {
                                Text("Mitglieder")
                            }
                            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                                val potentialUsers =
                                    (knownUsers + (shop.authorizedUsers ?: emptyList())).distinctBy(
                                        User::id
                                    )
                                potentialUsers.forEach {
                                    DropdownMenuItem(onClick = {
                                        //viewModel.pushEvent(UIEvent.AlertEvent("${it.name} ausgewählt"))
                                        if (selectedUsers.contains(it)) {
                                            selectedUsers.remove(it)
                                        } else {
                                            selectedUsers.add(it)
                                        }
                                    }) {
                                        if (selectedUsers.contains(it)) Icon(
                                            Icons.Filled.Check,
                                            "",
                                            tint = if (MaterialTheme.colors.isLight) Color.Black else Color.White,
                                            modifier = Modifier.padding(end = 7.dp)
                                        )
                                        Text(it.username)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Row {
                    Button(onClick = {
                        viewModel.deleteShop(context, shop.id)
                        navigator.navigate(HomeScreenDestination)
                    }, modifier = Modifier.padding(start = 10.dp, top = 10.dp)) {
                        Text("Löschen")
                    }
                    Button(onClick = {
                        editing = true
                        viewModel.editShop(context, shop.id, name, selectedUsers.map { it.id }) {
                            editing = false
                        }
                    }, modifier = Modifier.padding(start = 10.dp, top = 10.dp)) {
                        Text("Ändern")
                    }
                }
            }


        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Bearbeite Shop...", modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Du bist nicht der Besitzer dieses Shops!")
        }
    }
}