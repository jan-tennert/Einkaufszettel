package io.github.jan.einkaufszettel.android.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.common.components.PasswordField
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.repositories.UserStatus
import io.github.jan.supacompose.auth.auth


@Composable
@Destination
fun AccountScreen(viewModel: ProductViewModel) {
    val profile by viewModel.profileFlow.collectAsState()
    println(viewModel.supabaseClient.auth.currentSession.value?.accessToken)
    if (profile is UserStatus.Success) {
        var name by remember { mutableStateOf((profile as UserStatus.Success).profile.username) }
        var qrCode by rememberSaveable {
            mutableStateOf<Bitmap?>(null)
        }
        var showQrCode by remember { mutableStateOf(false) }
        var updating by remember { mutableStateOf(false) }
        var changePassword by remember { mutableStateOf(false) }
        if (!updating) {
            LaunchedEffect(Unit) {
                val encoder = BarcodeEncoder()
                qrCode = encoder.encodeBitmap(
                    (profile as UserStatus.Success).profile.id,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                val context = LocalContext.current
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
                        setClipboard(context, (profile as UserStatus.Success).profile.id)
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
                        val context = LocalContext.current
                        Button({
                            viewModel.clearCache(context)
                            viewModel.logout(context)
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
                }) {
                    Image(
                        qrCode!!.asImageBitmap(),
                        "QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                showQrCode = false
                            })
                }
            }
            if(changePassword) {
                Dialog({
                    changePassword = false
                }) {
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

fun setClipboard(context: Context, text: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
        clipboard.text = text
    } else {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }
}

fun getClipboard(context: Context): String {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
        clipboard.text?.toString() ?: ""
    } else {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val first = clipboard.primaryClip?.getItemAt(0)
        first?.text?.toString() ?: ""
    }
}