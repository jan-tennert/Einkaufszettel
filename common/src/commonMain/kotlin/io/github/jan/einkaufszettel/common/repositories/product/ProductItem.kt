package io.github.jan.einkaufszettel.common.repositories.product

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.jan.einkaufszettel.common.components.AsyncImage
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Shop(
    val id: Int,
    @SerialName("created_at")
    val createdAt: Instant,
    val name: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("authorized_users")
    val authorizedUsers: List<User>?,
    @SerialName("owner_name")
    val ownerName: String,
    val products: List<Product>?
) {

    @Composable
    fun Image(modifier: Modifier = Modifier) {
        AsyncImage(iconUrl, modifier)
    }

}

@Serializable
data class Product(
    val id: Int,
    @SerialName("created_at")
    val createdAt: Instant,
    val creator: User,
    val content: String,
    @SerialName("shop_id")
    val shopId: Int,
    @SerialName("done_since")
    val doneSince: Instant?,
    @SerialName("done_by")
    val doneBy: User?,
)

@Serializable
data class User(
    val id: String,
    val username: String,
)

@Serializable
data class AttachmentItem(val imageId: String, val extension: String)
