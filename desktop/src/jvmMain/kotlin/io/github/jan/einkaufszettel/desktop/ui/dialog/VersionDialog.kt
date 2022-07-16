package io.github.jan.einkaufszettel.desktop.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.github.jan.einkaufszettel.common.Constants
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import kotlinx.coroutines.launch

@Composable
fun VersionDialog(newVersion: Int, viewModel: ProductViewModel, disable: () -> Unit) {
    var text by remember { mutableStateOf("Die installierte Version ${Constants.VERSION} ist älter als die neuste Version $newVersion") }
    val scope = rememberCoroutineScope()
    val progress = remember { mutableStateOf(0f) }
    var downloading by remember { mutableStateOf(false) }
    Dialog(disable, resizable = false, title = "Neue Version verfügbar", state = rememberDialogState(height = 150.dp)) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text, fontSize = 16.sp, color = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
                if(!downloading) {
                    Button(onClick = {
                        downloading = true
                        text = "Lade neuste Version herunter..."
                        scope.launch {
                            viewModel.downloadUpdate(newVersion) {
                                progress.value = it
                            }
                        }
                    }, modifier = Modifier.padding(top = 15.dp)) {
                        Text("Updaten")
                    }
                } else {
                    LinearProgressIndicator(progress.value, modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(7.dp))
                }
            }
        }
    }
}