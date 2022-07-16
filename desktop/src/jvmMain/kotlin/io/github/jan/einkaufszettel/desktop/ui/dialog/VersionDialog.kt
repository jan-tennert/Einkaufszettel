package io.github.jan.einkaufszettel.desktop.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.github.jan.einkaufszettel.common.Constants
import java.awt.Desktop
import java.net.URI

@Composable
fun VersionDialog(newVersion: Int, disable: () -> Unit) {
    var text by remember { mutableStateOf("Die installierte Version ${Constants.VERSION} ist älter als die neuste Version $newVersion") }
    val scope = rememberCoroutineScope()
    val progress = remember { mutableStateOf(0f) }
    Dialog(disable, resizable = false, title = "Neue Version verfügbar", state = rememberDialogState(height = 150.dp)) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {
            Column {
                Text(text, fontSize = 16.sp)
                Button(onClick = {
                    Desktop.getDesktop().browse(URI("https://github.com/jan-tennert/Einkaufszettel/releases"))
                }, modifier = Modifier.padding(top = 15.dp)) {
                    Text("Updaten")
                }
            }
        }
    }
}