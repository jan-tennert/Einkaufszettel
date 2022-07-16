package io.github.jan.einkaufszettel.common.event

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

sealed interface UIEvent {

    @Composable
    fun Draw(disable: () -> Unit)

    class AlertEvent(private val text: String, private val onButtonClick: () -> Unit = {}) :
        UIEvent {

        @Composable
        override fun Draw(disable: () -> Unit) {
            Alert(disable, text = { Text(text) }, confirmButton = {
                Button({
                    onButtonClick()
                    disable()
                }) {
                    Text("Ok")
                }
            } )
        }

    }

    class PopupEvent(private val text: String) : UIEvent {

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun Draw(disable: () -> Unit) {
            LaunchedEffect(Unit) {
                delay(2000)
                disable()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                val shadow = animateDpAsState(16.dp)
                Card(
                    disable,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                        .clip(
                            RoundedCornerShape(20)
                        )
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text,
                            color = if (MaterialTheme.colors.isLight) Color.Black else Color.White,
                        )
                    }
                }
            }
        }

    }

}

@Composable
expect fun Alert(
    disable: () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
)