package io.github.jan.einkaufszettel.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.icons.PersonAdd
import io.github.jan.einkaufszettel.common.repositories.product.User

enum class UserCardMode {
    Known,
    Shop
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserCard(user: User, mode: UserCardMode, deleteUser: (id: String) -> Unit, resolveAndAdd: (id: String) -> Unit, copyToClipboard: (id: String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f)
            .padding(10.dp)
            .combinedClickable(onLongClick = {
                copyToClipboard(user.id)
                //viewModel.pushEvent(UIEvent.PopupEvent("Id kopiert"))
            }) {  },
        backgroundColor = MaterialTheme.colors.background
    ) {
        Column {
            Text(user.username, fontSize = 20.sp)
            Text("Id: ${user.id}", fontSize = 10.sp)
        }
        when(mode) {
            UserCardMode.Known -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton({
                        deleteUser(user.id)
                    }) {
                        Icon(
                            Icons.Filled.Delete, contentDescription = "", modifier = Modifier
                                .size(40.dp)
                        )
                    }
                }
            }
            UserCardMode.Shop -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton({
                        resolveAndAdd(user.id)
                    }) {
                        Icon(
                            MIcon.PersonAdd, contentDescription = "", modifier = Modifier
                                .size(40.dp)
                        )
                    }
                }
            }
        }
    }
}