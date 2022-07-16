package io.github.jan.einkaufszettel.android.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@Composable
@Destination
fun SettingsScreen(
    viewModel: ProductViewModel
) {
    var alert by remember { mutableStateOf<String?>(null) }
    var showDataDeleteScreen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column {
        Button(onClick = {
            showDataDeleteScreen = true
        }, modifier = Modifier.padding(start = 10.dp)) {
            Text("Daten löschen")
        }
    }

    if(alert != null) {
        AlertDialog(onDismissRequest = { alert = null }, confirmButton = {
            Button({
                alert = null
            }) {
                Text("Ok")
            }
        }, text = {
            if(alert != null) {
                Text(alert!!)
            }
        })
    }

    if(showDataDeleteScreen) {
        AlertDialog(onDismissRequest = {
            showDataDeleteScreen = false
        }, buttons = {
            Row(modifier = Modifier.padding(start = 10.dp)) {
                Button({
                    scope.launch {
                        viewModel.clearCache(context)
                    }
                }) {
                    Text("Löschen")
                }
                Button({
                    showDataDeleteScreen = false
                }, modifier = Modifier.padding(start = 10.dp)) {
                    Text("Abbrechen")
                }
            }
        }, title = {
            Text("Möchtest du die Daten wirklich löschen? Dies wird die App neustarten.")
        })
    }
}