package io.github.jan.einkaufszettel.desktop.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun FileChooser(
    title: String,
    mode: Int,
    additional: FileDialog.() -> Unit = {},
    onFinish: (selectedFiles: List<File>) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog((null as Frame?), title, mode) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onFinish(files.toList())
                }
            }
        }.apply(additional)
    },
    dispose = FileDialog::dispose
)