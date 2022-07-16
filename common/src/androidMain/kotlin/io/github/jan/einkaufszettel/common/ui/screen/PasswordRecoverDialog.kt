package io.github.jan.einkaufszettel.common.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.jan.supacompose.postgrest.library.R

@Composable
actual fun PasswordRecoverDialog(disable: () -> Unit, sendPasswordRecovery: (String) -> Unit) {
    Dialog(disable) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)) {
            var recoveryEmail by remember { mutableStateOf("") }
            TextField(recoveryEmail, { recoveryEmail = it }, label = { Text("Email") },
                modifier = Modifier.padding(10.dp), singleLine = true)
            Button({
                sendPasswordRecovery(recoveryEmail)
                disable()
            }, enabled = recoveryEmail.length >= 6) {
                Text("Wiederherstellungs-Link senden")
            }
        }
    }
}

@Composable
actual fun getGoogleLogo() = painterResource(R.drawable.googleg_standard_color_18)