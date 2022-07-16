package io.github.jan.einkaufszettel.desktop.ui.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val DrawerShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Rectangle(Rect(0f, 0f, size.width / 4, size.height))
    }
}