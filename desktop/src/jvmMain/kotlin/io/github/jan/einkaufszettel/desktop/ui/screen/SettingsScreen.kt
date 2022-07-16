package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.einkaufszettel.desktop.ProductViewModel

@Composable
fun SettingsScreen(
    viewModel: ProductViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            var darkMode by remember { mutableStateOf(settings.darkMode) }
            Switch(darkMode, { darkMode = it; viewModel.updateSettings(settings.copy(darkMode = darkMode)) })
            Text(
                text = "Dunkler Modus",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

    }
}