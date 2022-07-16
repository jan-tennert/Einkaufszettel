package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
actual fun DeleteDialog(onDelete: () -> Unit, disable: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = {
        disable()
        onCancel()
    }) {
        Column(
            modifier = Modifier.background(
                MaterialTheme.colors.background,
            )
        ) {
            Text("Möchtest du diesen Eintrag wirklich löschen?", modifier = Modifier.padding(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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