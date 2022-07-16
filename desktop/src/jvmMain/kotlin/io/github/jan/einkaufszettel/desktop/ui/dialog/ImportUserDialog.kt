package io.github.jan.einkaufszettel.desktop.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.github.jan.einkaufszettel.desktop.ProductViewModel

@Composable
fun ImportUserDialog(viewModel: ProductViewModel, disable: () -> Unit) {
    Dialog(disable, resizable = false, title = "User hinzufügen", state = rememberDialogState(height = 150.dp)) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            var idError by remember { mutableStateOf<String?>(null) }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                var id by remember { mutableStateOf("") }
                TextField(id, { id = it }, label = { Text("User Id") }, placeholder = { Text("1aba757d-464a-46fc-af62-70b8a1da4ea7") }, isError = idError != null)
                if(idError != null) {
                    Text(
                        text = idError!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Button({
                    viewModel.resolveAndAddUser(id, { disable() }) {
                        idError = "Benutzer nicht gefunden"
                    }
                }) {
                    Text("Speichern")
                }
            }
        }
    }
}