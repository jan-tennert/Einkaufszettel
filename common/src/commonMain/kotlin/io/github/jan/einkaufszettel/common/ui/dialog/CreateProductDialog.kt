package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.runtime.Composable
import io.github.jan.einkaufszettel.common.repositories.product.Product

@Composable
expect fun CreateProductDialog(value: String = "", shop: Int, edit: Product? = null, onDone: (content: String) -> Unit, disable: () -> Unit)