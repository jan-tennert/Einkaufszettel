package io.github.jan.einkaufszettel.android.ui.dialog

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.android.ui.screen.CameraPreview
import io.github.jan.einkaufszettel.android.ui.screen.getClipboard
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import java.util.*

enum class ImportType {
    QR_CODE,
    MANUAL,
    NOT_SET
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImportUserDialog(viewModel: ProductViewModel, disable: () -> Unit) {
    Dialog(disable) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)) {
            var importType by remember { mutableStateOf(ImportType.NOT_SET) }
            if(importType == ImportType.NOT_SET) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Button({
                        importType = ImportType.MANUAL
                    }){
                        Text("Manuell eingeben")
                    }
                    Button({
                        importType = ImportType.QR_CODE
                    }){
                        Text("QR Code scannen")
                    }
                }
            } else {
                when(importType) {
                    ImportType.QR_CODE -> {
                        val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
                        val context = LocalContext.current
                        SideEffect {
                            if (!cameraPermissionState.hasPermission) {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                        CameraPreview {
                            try {
                                UUID.fromString(it)
                                viewModel.resolveAndAddUser(it, context)
                                disable()
                                true
                            } catch(e: IllegalArgumentException) {
                                viewModel.pushEvent(UIEvent.AlertEvent("Kein gültiger QR Code"))
                                false
                            }
                        }
                    }
                    ImportType.MANUAL -> {
                        val context = LocalContext.current
                        var id by remember { mutableStateOf(getClipboard(context)) }
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            TextField(id, { id = it })
                            Button({
                                try {
                                    UUID.fromString(id)
                                    viewModel.resolveAndAddUser(id, context)
                                    disable()
                                } catch(_: IllegalArgumentException) {
                                    viewModel.pushEvent(UIEvent.AlertEvent("Keine gültige Id"))
                                }
                            }) {
                                Text("Hinzufügen")
                            }
                        }
                    }
                }
            }
        }
    }
}