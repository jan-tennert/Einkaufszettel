package io.github.jan.einkaufszettel.desktop.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.einkaufszettel.common.components.UserCard
import io.github.jan.einkaufszettel.common.components.UserCardMode
import io.github.jan.einkaufszettel.desktop.ProductViewModel
import io.github.jan.einkaufszettel.desktop.ui.dialog.ImportUserDialog
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun KnownUsersScreen(viewModel: ProductViewModel) {
    val knownUsers by viewModel.knownUsers.collectAsState()
    var openCreateScreen by remember { mutableStateOf(false) }
    LazyColumn {
        items(knownUsers.size, { knownUsers[it].id }) {
            val user = knownUsers[it]
            UserCard(user, UserCardMode.Known, { id -> viewModel.removeKnownUser(id) }, { id -> viewModel.resolveAndAddUser(id) }, {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard;
                clipboard.setContents(StringSelection(user.id), null);
            })
        }
    }

    if(!openCreateScreen) {
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(onClick = {
                openCreateScreen = true
            }, modifier = Modifier.offset(x = (-10).dp, y = (-10).dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
    if(openCreateScreen) {
        ImportUserDialog(viewModel) {
            openCreateScreen = false
        }
    }
}