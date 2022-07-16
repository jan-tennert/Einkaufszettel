package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import io.github.g0dkar.qrcode.QRCode
import io.github.jan.einkaufszettel.common.components.PasswordField
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.supacompose.auth.auth
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun AccountScreen(viewModel: ProductViewModel) {
    val profile by viewModel.profileFlow.collectAsState()
    println(viewModel.supabaseClient.auth.currentSession.value?.accessToken)
    if (profile is UserStatus.Success) {
        var name by remember { mutableStateOf((profile as UserStatus.Success).profile.username) }
        var qrCode by rememberSaveable {
            mutableStateOf<ImageBitmap?>(null)
        }
        var showQrCode by remember { mutableStateOf(false) }
        var updating by remember { mutableStateOf(false) }
        var changePassword by remember { mutableStateOf(false) }
        if (!updating) {
            LaunchedEffect(Unit) {
                qrCode = org.jetbrains.skia.Image.makeFromEncoded(QRCode((profile as UserStatus.Success).profile.id)
                    .render()
                    .getBytes()).toComposeImageBitmap()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column {
                    TextField(
                        name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.padding(bottom = 10.dp),
                        singleLine = true
                    )
                    Text("Id: ${(profile as UserStatus.Success).profile.id}", fontSize = 10.sp)
                    Button({
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard;
                        clipboard.setContents(StringSelection((profile as UserStatus.Success).profile.id), null);
                        viewModel.pushEvent(UIEvent.PopupEvent("ID kopiert"))
                    }) {
                        Text("Eigene Id kopieren")
                    }
                    Button({
                        showQrCode = true
                    }) {
                        Text("QR Code anzeigen")
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Column {
                    Button({
                        changePassword = true
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Passwort ändern")
                    }
                    Row {
                        Button({
                            viewModel.logout()
                        }, modifier = Modifier.padding(end = 7.dp)) {
                            Text("Ausloggen")
                        }
                        Button({
                            updating = true
                            viewModel.updateUsername((profile as UserStatus.Success).profile.id, name) {
                                updating = false
                            }
                        }) {
                            Text("Speichern")
                        }
                    }
                }
            }
            if (showQrCode) {
                Dialog({
                    showQrCode = false
                }, title = "Id QR Code", state = rememberDialogState(height = 400.dp)) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                        Image(
                            qrCode!!,
                            "QR Code",
                            modifier = Modifier
                                .fillMaxSize(0.9f)
                                .align(Alignment.Center))
                    }
                }
            }
            if(changePassword) {
                Dialog({
                    changePassword = false
                }, state = rememberDialogState(height = 170.dp), title = "Passwort ändern", resizable = false) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.background)) {
                            var newPassword by remember { mutableStateOf("") }
                            PasswordField(newPassword, { newPassword = it }, label = "Neues Passwort",
                                modifier = Modifier.padding(10.dp))
                            Button({
                                viewModel.changePasswordTo(newPassword)
                                changePassword = false
                            }, enabled = newPassword.length >= 6) {
                                Text("Ändern")
                            }
                        }
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Speichere Profil...", modifier = Modifier.padding(top = 10.dp))
                }
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Nicht eingeloggt", modifier = Modifier.align(Alignment.Center))
        }
    }
}