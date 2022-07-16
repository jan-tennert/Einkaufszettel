package io.github.jan.einkaufszettel.android.ui.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.android.getBytesFromUri
import io.github.jan.einkaufszettel.android.getExtensionFromUri
import io.github.jan.einkaufszettel.android.ui.dialog.CardCreateDialog
import io.github.jan.einkaufszettel.android.ui.dialog.CardEditDialog
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.components.CardCard
import io.github.jan.einkaufszettel.common.repositories.card.Card

@Composable
@Destination
fun CardsScreen(viewModel: ProductViewModel) {
    LocalContext.current
    val cards by viewModel.cardFlow.collectAsState(emptyList())
    var showEditScreen by remember { mutableStateOf<Card?>(null) }
    var showCreateScreen by rememberSaveable { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    if(selectedCard != null) {
        val scale = remember { mutableStateOf(1f) }
        Dialog(onDismissRequest = { selectedCard = null}) {
            Box(
                modifier = Modifier
                    .clip(RectangleShape) // Clip the box content
                    .fillMaxSize() // Give the size you want...
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            println("cen $centroid")
                            println("pan $pan")
                            scale.value *= zoom
                        }
                    }
            ) {
                selectedCard?.Image(viewModel.supabaseClient, Modifier
                    .fillMaxSize()
                    .align(Alignment.Center) // keep the image centralized into the Box
                    .graphicsLayer(
                        // adding some zoom limits (min 50%, max 200%)
                        scaleX = maxOf(.5f, minOf(3f, scale.value)),
                        scaleY = maxOf(.5f, minOf(3f, scale.value)),
                    ))
            }
            Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "", modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        selectedCard = null
                    })
            }
        }
    } else {
        val ownCards = remember(cards) { cards.filter(Card::isOwner) }
        val otherCards = remember(cards) { (cards - ownCards.toSet()) }
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    "Eigene Karten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            ownCards.chunked(2).forEach {
                item {
                    Row {
                        for (card in it) {
                            CardCard(card, viewModel.supabaseClient, { selectedCard = card }) {
                                showEditScreen = card
                            }
                        }
                    }
                }
            }
            item {
                Text("Freigeschaltete Karten", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
            otherCards.chunked(2).forEach {
                item {
                    Row {
                        for (card in it) {
                            CardCard(card, viewModel.supabaseClient, { selectedCard = card }) {
                                showEditScreen = card
                            }
                        }
                    }
                }
            }
        }

        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(onClick = {
                showCreateScreen = true
            }, shape = RoundedCornerShape(100), modifier = Modifier.offset(x = (-10).dp, y = (-10).dp)) {
                Icon(Icons.Filled.Add, contentDescription = "")
            }
        }
    }

    if(showCreateScreen) {
        CardCreateDialog(viewModel) { showCreateScreen = false }
    }
    if(showEditScreen != null) {
        CardEditDialog(viewModel, showEditScreen!!) { showEditScreen = null }
    }
}

fun Uri.uploadImageAndSave(context: Context, viewModel: ProductViewModel) {
    val extensions = context.getExtensionFromUri(this)
    val bytes = context.getBytesFromUri(this)!!
  //  viewModel.uploadAttachment(UUID.randomUUID().toString(), extensions, bytes, context)
}
