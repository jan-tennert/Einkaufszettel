package io.github.jan.einkaufszettel.desktop.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
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
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.QuestionMark
import io.github.jan.einkaufszettel.common.repositories.card.Card
import io.github.jan.einkaufszettel.common.repositories.product.User
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.components.FileChooser
import java.awt.FileDialog
import java.io.File

@Composable
fun CardCreateDialog(viewModel: ProductViewModel, disable: () -> Unit) {
    var image by rememberSaveable { mutableStateOf<File?>(null) }
    var description by rememberSaveable { mutableStateOf("") }
    var showFileChooser by rememberSaveable { mutableStateOf(false) }
    Dialog(disable, resizable = false, state = rememberDialogState(height = 300.dp), title = "Karte erstellen") {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            val selectedUsers = remember { mutableStateListOf<User>() }
            var expanded by remember { mutableStateOf(false) }
            TextField(
                description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier
                    .padding(bottom = 10.dp, top = 10.dp)
                    .align(
                        Alignment.CenterHorizontally
                    ),
                singleLine = true
            )
            Box(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    showFileChooser = true
                }
            ) {
                if (image == null) {
                    Icon(
                        MIcon.QuestionMark,
                        "",
                        modifier = Modifier
                            .size(100.dp)
                            .border(
                                2.dp,
                                if (MaterialTheme.colors.isLight) Color.Black else Color.White
                            )
                            .align(Alignment.Center),
                        tint = if (MaterialTheme.colors.isLight) Color.Black else Color.White
                    )
                } else {
                    val bitmap = remember(image) { loadImageBitmap(image!!.inputStream()) }
                    Image(
                        bitmap, "", modifier = Modifier
                            .size(100.dp)
                            .border(
                                2.dp,
                                if (MaterialTheme.colors.isLight) Color.Black else Color.White
                            )
                            .align(Alignment.Center)
                    )
                }
            }
            /*Button(
                onClick = {
                    // launcher.launch("image/*")
                }, modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Bild auswählen")
            }
            */
             */
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
            Button(
                {
                    viewModel.createCard(description, selectedUsers.map(User::id), image!!)
                    disable()
                },
                enabled = description.isNotBlank() && image != null,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Erstellen")
            }
        }
    }
    if (showFileChooser) {
        FileChooser("Wähle ein Bild aus", FileDialog.LOAD, { file = "*.jpg;*.jpeg,*.png" }) {
            showFileChooser = false
            image = it.firstOrNull()
        }
    }
}

@Composable
fun CardEditDialog(viewModel: ProductViewModel, card: Card, disable: () -> Unit) {
    var description by rememberSaveable { mutableStateOf(card.description) }
    val knownUsers by viewModel.knownUsers.collectAsState()
    val selectedUsers = remember {
        mutableStateListOf<User>().apply {
            addAll(card.authorizedUsers ?: emptyList())
        }
    }
    var expanded by remember { mutableStateOf(false) }
    Dialog(disable, resizable = false, title = "Karte bearbeiten") {
        Column(
            modifier = Modifier
                .size(width = 400.dp, height = 280.dp)
                .background(MaterialTheme.colors.background)
        ) {
            TextField(
                description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier
                    .padding(bottom = 10.dp, top = 10.dp)
                    .align(
                        Alignment.CenterHorizontally
                    ),
                singleLine = true
            )
            card.Image(
                viewModel.supabaseClient, Modifier
                    .size(100.dp)
                    .border(
                        2.dp,
                        if (MaterialTheme.colors.isLight) Color.Black else Color.White
                    )
                    .align(Alignment.CenterHorizontally)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box {
                    Button({
                        expanded = !expanded
                    }, modifier = Modifier) {
                        Text("Mitglieder")
                    }
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        val potentialUsers =
                            (knownUsers + (card.authorizedUsers ?: emptyList())).distinctBy(
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
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Button({
                    viewModel.deleteCard(card.id, card.imagePath)
                    disable()
                }, modifier = Modifier.padding(end = 10.dp)) {
                    Text("Löschen")
                }
                Button({
                    viewModel.editCard(card.id, description, selectedUsers.map(User::id))
                    disable()
                }) {
                    Text("Speichern")
                }
            }
        }
    }
}
