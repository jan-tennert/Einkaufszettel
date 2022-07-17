package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import io.github.jan.supacompose.auth.user.UserSession

@Composable
actual fun ProfileDialog(
    session: UserSession,
    createProfile: (name: String) -> Unit,
    logout: () -> Unit,
    disable: () -> Unit
) {
    Dialog(onDismissRequest = disable) {
        Column {
            var username by remember { mutableStateOf("") }
            Text("Bitte gib einen Namen ein um dein Profil zu erstellen:", modifier = Modifier.align(
                Alignment.CenterHorizontally).padding(bottom = 13.dp))
            TextField(username, { username = it }, label = { Text("Name") })
            TextField(username, { username = it }, label = { Text("Name") })
            Row {
                Button(onClick = {
                    createProfile(username)
                }) {
                    Text("Erstellen")
                }
                Button(onClick = {
                    logout()
                }, modifier = Modifier.padding(start = 10.dp)) {
                    Text("Account wechseln")
                }
            }
        }
    }
}