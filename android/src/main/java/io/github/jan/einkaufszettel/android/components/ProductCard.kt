package io.github.jan.einkaufszettel.android.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soywiz.klock.DateTimeTz
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.icons.CheckBox
import io.github.jan.einkaufszettel.common.icons.CheckBoxOutlineBlank
import io.github.jan.einkaufszettel.common.icons.DragHandle
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.repositories.product.Product
import io.github.jan.einkaufszettel.common.ui.dialog.CreateProductDialog
import io.github.jan.einkaufszettel.common.ui.dialog.DeleteDialog
import io.github.jan.supacompose.auth.auth
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItemCard(
    isDragging: Boolean,
    item: Product,
    reorderable: ReorderableLazyListState,
    showMoveButton: Boolean,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = viewModel()
) {
    var showDeleteScreen by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }
    var loadingDone by remember(item) { mutableStateOf(false) }
    var loadingDelete by remember(item) { mutableStateOf(false) }
    val context = LocalContext.current
    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f)
            .shadow(elevation.value)
            .padding(10.dp)
            .then(modifier),
        //     .animateItemPlacement(),
        backgroundColor = MaterialTheme.colors.background
    ) {
        Row {
            if(showMoveButton) {
                Icon(MIcon.DragHandle, "",
                    Modifier
                        .size(30.dp)
                        .align(CenterVertically)
                        .detectReorder(reorderable))
            } else {
                if(loadingDone) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                } else {
                    if(item.doneBy == null) {
                        IconButton({
                            loadingDone = true
                            productViewModel.markProductAsDone(
                                context,
                                item.id,
                                productViewModel.supabaseClient.auth.currentSession.value!!.user!!.id
                            ) {
                                loadingDone = false
                            }
                        }, modifier = Modifier.align(CenterVertically)) {
                            Icon(
                                MIcon.CheckBoxOutlineBlank, contentDescription = "", modifier = Modifier
                                    .size(30.dp)
                            )
                        }
                    } else {
                        IconButton({
                            loadingDone = true
                            productViewModel.markProductAsNotDone(
                                context,
                                item.id,
                            ) {
                                loadingDone = false
                            }
                        }, modifier = Modifier.align(CenterVertically)) {
                            Icon(
                                MIcon.CheckBox,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(30.dp)
                            )
                        }
                    }
                }
            }
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.combinedClickable(onLongClick = {
                showEditScreen = true
            }) {

            }) {
                val formattedDate = DateTimeTz.fromUnix(item.createdAt.toEpochMilliseconds()).format("dd.MM.yyyy HH:mm")
                val formattedDone = item.doneSince?.toEpochMilliseconds()
                    ?.let { DateTimeTz.fromUnix(it).format("dd.MM.yyyy HH:mm") }
                Column(modifier = Modifier.padding(start = if(!showMoveButton) 14.dp else 10.dp, bottom = 10.dp, top = 10.dp)) {
                    Text(item.content, fontSize = 20.sp, style = TextStyle(textDecoration = if(item.doneBy != null) TextDecoration.LineThrough else null), modifier = Modifier.fillMaxWidth(0.78f))
                    Text(buildAnnotatedString {
                        append(formattedDone ?: formattedDate)
                        if(formattedDone != null) {
                            withStyle(SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)) {
                                append(" durchgestrichen von ")
                            }
                            append(item.doneBy?.username ?: "")
                        } else {
                            withStyle(SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)) {
                                append(" von ")
                            }
                            append(item.creator.username)
                        }
                    }, fontSize = 10.sp)
                }
                Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxSize()) {
                    if(loadingDelete) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        IconButton({
                            showDeleteScreen = true
                        }) {
                            Icon(
                                Icons.Filled.Delete, contentDescription = "", modifier = Modifier
                                    .size(40.dp))
                        }
                    }
                }
            }
        }
    }

    if(showDeleteScreen) {
        DeleteDialog(
            onDelete = {
                loadingDelete = true
                productViewModel.deleteProduct(context, item.id)
            },
            disable = { showDeleteScreen = false },
            onCancel = {
                loadingDelete = false
            }
        )
    }

    if(showEditScreen) {
        CreateProductDialog(shop = item.shopId, disable = { showEditScreen = false }, edit = item, onDone = { productViewModel.editProductContent(context, item.id, it) })
    }

}