package io.github.jan.einkaufszettel.android.ui.dialog

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import io.github.jan.einkaufszettel.android.AutoUpdater
import io.github.jan.einkaufszettel.common.Constants
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VersionDialog(newVersion: Int) {
    var downloading by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("Die installierte Version ${Constants.VERSION} ist älter als die neuste Version $newVersion") }
    val storagePermissionState = rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val autoUpdater: AutoUpdater by inject()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val progress = remember { mutableStateOf(0f) }
    SideEffect {
        storagePermissionState.launchPermissionRequest()
    }
    if(storagePermissionState.hasPermission) {
        Dialog(onDismissRequest = {}) {
            Column {
                Text(text, fontSize = 16.sp)
                if(!downloading) {
                    Button(onClick = {
                        downloading = true
                        text = "Lade neuste Version herunter..."
                        scope.launch {
                            autoUpdater.enqueueDownload(context, progress)
                        }
                    }, modifier = Modifier.padding(top = 15.dp)) {
                        Text("Updaten")
                    }
                } else {
                    LinearProgressIndicator(progress.value / 100, modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(7.dp))
                }
            }
        }
    }
}