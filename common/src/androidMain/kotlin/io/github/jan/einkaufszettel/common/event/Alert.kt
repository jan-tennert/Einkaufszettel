package io.github.jan.einkaufszettel.common.event

import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable

@Composable
actual fun Alert(
    disable: () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) = AlertDialog(disable, confirmButton, text = text)