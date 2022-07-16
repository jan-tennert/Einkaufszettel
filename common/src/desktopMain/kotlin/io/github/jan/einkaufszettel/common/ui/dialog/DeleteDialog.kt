package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState

@Composable
actual fun DeleteDialog(onDelete: () -> Unit, disable: () -> Unit, onCancel: () -> Unit) {
    Dialog(onCloseRequest = {
        disable()
        onCancel()
    }, state = rememberDialogState(height = 150.dp), title = "Produkt löschen", resizable = false) {
        Box(modifier = Modifier.fillMaxSize().background(
            MaterialTheme.colors.background,
        )) {
            Column {
                Text("Möchtest du diesen Eintrag wirklich löschen?", modifier = Modifier.padding(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)
                ) {
                    Button(onClick = {
                        onDelete()
                        disable()
                    }) {
                        Text("Löschen")
                    }
                    Button(onClick = {
                        disable()
                        onCancel()
                    }, modifier = Modifier.padding(start = 10.dp)) {
                        Text("Abbrechen")
                    }
                }
            }
        }
    }
}