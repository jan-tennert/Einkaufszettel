package io.github.jan.einkaufszettel.android.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.QuestionMark
import io.github.jan.einkaufszettel.common.repositories.product.User

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Destination
fun ShopCreateScreen(viewModel: ProductViewModel) {
    var image by rememberSaveable { mutableStateOf<Uri?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if(uri != null) {
            image = uri
        } else {
            viewModel.pushEvent(UIEvent.AlertEvent("Kein Bild ausgew??hlt"))
        }
    }
    var creating by rememberSaveable { mutableStateOf(false) }
    if(!creating) {
        val selectedUsers = remember { mutableStateListOf<User>() }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column() {
                var expanded by remember { mutableStateOf(false) }
                TextField(name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.padding(bottom = 10.dp).align(Alignment.CenterHorizontally), singleLine = true)
                if(image == null) {
                    Icon(
                        MIcon.QuestionMark, "", modifier = Modifier
                        .size(100.dp)
                        .border(
                            2.dp,
                            if (MaterialTheme.colors.isLight) Color.Black else Color.White
                        )
                        .align(CenterHorizontally), tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                } else {
                    AsyncImage(image, "", modifier = Modifier
                        .size(100.dp)
                        .border(
                            2.dp,
                            if (MaterialTheme.colors.isLight) Color.Black else Color.White
                        )
                        .align(CenterHorizontally))
                }
                Button(onClick = {
                    launcher.launch("image/*")
                }, modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp)
                    .align(CenterHorizontally)) {
                    Text("Bild ausw??hlen")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box {
                        Button({
                            expanded = !expanded
                        }, modifier = Modifier) {
                            Text("Mitglieder")
                        }
                        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            val knownUsers by viewModel.knownUsers.collectAsState()
                            knownUsers.forEach {
                                DropdownMenuItem(onClick = {
                                    //viewModel.pushEvent(UIEvent.AlertEvent("${it.name} ausgew??hlt"))
                                    if(selectedUsers.contains(it)) {
                                        selectedUsers.remove(it)
                                    } else {
                                        selectedUsers.add(it)
                                    }
                                }) {
                                    if(selectedUsers.contains(it)) Icon(Icons.Filled.Check, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White, modifier = Modifier.padding(end = 7.dp))
                                    Text(it.username)
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Button(onClick = {
                creating = true
                viewModel.createShop(context, name, image!!, selectedUsers.map(User::id)) {
                    creating = false
                }
            }, modifier = Modifier.padding(start = 10.dp, top = 10.dp), enabled = name.isNotBlank() && image != null) {
                Text("Erstellen")
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = CenterHorizontally) {
                CircularProgressIndicator()
                Text("Erstelle Shop...", modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}