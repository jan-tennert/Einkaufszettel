package io.github.jan.einkaufszettel.common.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState

@Composable
actual fun Alert(
    disable: () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) = Dialog(disable, resizable = false, title = "Info", state = rememberDialogState(height = 150.dp)) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            text()
            confirmButton()
        }
    }
}