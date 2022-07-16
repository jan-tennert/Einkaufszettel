package io.github.jan.einkaufszettel.common.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    text: String = "Sign Up with Google",
    loadingText: String = "Creating Account...",
    icon: @Composable() () -> Unit,
    borderColor: Color = Color.LightGray,
    backgroundColor: Color = MaterialTheme.colors.surface,
    progressIndicatorColor: Color = MaterialTheme.colors.primary,
    onClicked: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.clickable { clicked = !clicked },
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(width = 1.dp, color = borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            /*Icon(
                painter = painterResource(id = icon),
                contentDescription = "Google Button",
                tint = Color.Unspecified
            )*/
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (clicked) loadingText else text)
            if (clicked) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp,
                    color = progressIndicatorColor
                )
                onClicked()
            }
        }
    }
}