package io.github.jan.einkaufszettel.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.einkaufszettel.common.repositories.card.Card
import io.github.jan.supacompose.SupabaseClient

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCard(card: Card, supabaseClient: SupabaseClient, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Card(
            backgroundColor = MaterialTheme.colors.background,
            modifier = Modifier.padding(10.dp).combinedClickable(onLongClick = {
                if (card.isOwner) onLongClick()
            }, onClick = onClick)
        ) {
            Column {
                card.Image(supabaseClient, Modifier.size(150.dp))
                Text(text = card.description, modifier = Modifier.align(CenterHorizontally))
                Text(text = "von ${card.owner.username}", modifier = Modifier.align(CenterHorizontally), fontSize = 10.sp)
            }
        }
    }
}