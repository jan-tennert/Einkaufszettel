package io.github.jan.einkaufszettel.common.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
actual fun PasswordRecoverDialog(disable: () -> Unit, sendPasswordRecovery: (String) -> Unit) {
    throw IllegalStateException("Not supported on desktop")
}

@Composable
actual fun getGoogleLogo() = painterResource("google_logo.xml")