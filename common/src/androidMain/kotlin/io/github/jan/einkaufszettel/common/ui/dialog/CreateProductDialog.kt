package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.jan.einkaufszettel.common.Suggestions
import io.github.jan.einkaufszettel.common.repositories.product.Product

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun CreateProductDialog(
    value: String,
    shop: Int,
    edit: Product?,
    onDone: (content: String) -> Unit,
    disable: () -> Unit
) {
    val context = LocalContext.current
    Dialog(disable) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)) {
            var content by remember { mutableStateOf(if(edit != null) TextFieldValue(edit.content, TextRange(edit.content.length)) else TextFieldValue(value)) }
            Column {
                var suggestions by remember { mutableStateOf(emptyList<String>()) }
                var isFocused by remember { mutableStateOf(false) }
                var autoCompleteFinished by remember { mutableStateOf(false) }
                TextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if(!autoCompleteFinished) {
                            isFocused = true
                            suggestions = Suggestions.getSuggestions(content.text)
                        } else {
                            autoCompleteFinished = false
                        }
                    },
                    label = { Text("Produkt") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(content.text.isNotEmpty()) {
                            onDone(content.text)
                            disable()
                        } else {
                            //viewModel.errorFlow.value = ImportantError("Produkt darf nicht leer sein", Exception())
                        }
                    }), modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(MaterialTheme.colors.background)
                        .onFocusChanged {
                            isFocused = it.isFocused
                        })
                if(isFocused) {
                    LazyColumn {
                        items(suggestions.size) {
                            val suggestion = suggestions[it]
                            ListItem(modifier = Modifier
                                .clickable {
                                    content =
                                        TextFieldValue(suggestion, TextRange(suggestion.length))
                                    autoCompleteFinished = true
                                    isFocused = false
                                }
                                .fillMaxWidth(0.85f)
                                .background(MaterialTheme.colors.background)) {
                                Text(suggestion, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
            Icon(Icons.Filled.Done, "", modifier = Modifier
                .clickable {
                    if(content.text.isNotEmpty()) {
                        onDone(content.text)
                        disable()
                    } else {
                        //viewModel.errorFlow.value = ImportantError("Produkt darf nicht leer sein", Exception())
                    }
                }
                .padding(start = 10.dp)
                .size(50.dp)
                .fillMaxWidth(0.25f)
                .background(MaterialTheme.colors.background))
        }
    }
}