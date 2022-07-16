package io.github.jan.einkaufszettel.android.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.einkaufszettel.common.icons.Help
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.repositories.barcode.Allergen
import io.github.jan.einkaufszettel.common.repositories.barcode.BarcodeProduct

@Composable
fun ProductInfoCard(product: BarcodeProduct, onCreate: () -> Unit) {
    val darkMode = !MaterialTheme.colors.isLight
    Column(modifier = Modifier.padding(10.dp )) {
        Row() {
            if(product.imageUrl != null) {
                AsyncImage(product.imageUrl, product.name, modifier = Modifier
                    .scale(1f)
                    .size(70.dp)
                    .clip(RoundedCornerShape(20)))
            } else {
                Icon(MIcon.Help, "", tint = if (darkMode) Color.White else Color.Black, modifier = Modifier.size(70.dp))
            }
            Text(product.name, modifier = Modifier.padding(start = 7.dp))
        }
        val color = if(darkMode) Color.White else Color.Black
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val hasGluten = product.allergens?.contains(Allergen.GLUTEN)
            Icon(if(hasGluten != null && hasGluten && product.imageUrl != null) Icons.Filled.Close else if(hasGluten != null && !hasGluten && product.imageUrl != null) Icons.Filled.CheckCircle else MIcon.Help, "", tint = if (hasGluten != null && product.imageUrl != null) if(hasGluten) Color.Red else Color.Green else color)
            Text("Glutenfrei", modifier = Modifier.padding(start = 8.dp))
        }
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            val hasMilk = product.allergens?.contains(Allergen.MILK)
            Icon(if(hasMilk != null && hasMilk && product.imageUrl != null) Icons.Filled.Close else if(hasMilk != null && !hasMilk && product.imageUrl != null) Icons.Filled.CheckCircle else MIcon.Help, "", tint = if (hasMilk != null && product.imageUrl != null) if(hasMilk) Color.Red else Color.Green else color)
            Text("Milchfrei", modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(product.allergenInfo) {
                Row(Modifier.padding(8.dp),verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(6.dp)){
                        drawCircle(if(darkMode) Color.White else Color.Black)
                    }
                    Text(text = it, fontSize = 12.sp)
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Button(onCreate) {
                Text("In die Einkaufsliste")
            }
        }
    }
}