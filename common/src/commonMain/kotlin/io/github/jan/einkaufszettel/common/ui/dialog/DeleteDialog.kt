package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.runtime.Composable

@Composable
expect fun DeleteDialog(onDelete: () -> Unit, disable: () -> Unit, onCancel: () -> Unit)