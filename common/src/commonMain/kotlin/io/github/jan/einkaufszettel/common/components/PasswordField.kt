package io.github.jan.einkaufszettel.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.Visibility
import io.github.jan.einkaufszettel.common.icons.VisibilityOff

@Composable
fun PasswordField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        var visible by remember { mutableStateOf(false) }
        TextField(password, onValueChange = onPasswordChanged, label = { Text(label)}, visualTransformation = if(visible) VisualTransformation.None else PasswordVisualTransformation(), singleLine = true)
        IconButton({
            visible = !visible
        }, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(if(visible) MIcon.Visibility else MIcon.VisibilityOff, "", tint = if(MaterialTheme.colors.isLight) Color.Black else Color.White)
        }
    }
}