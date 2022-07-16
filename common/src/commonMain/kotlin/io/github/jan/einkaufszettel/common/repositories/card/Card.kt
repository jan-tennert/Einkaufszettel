package io.github.jan.einkaufszettel.common.repositories.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.jan.einkaufszettel.common.components.AsyncImage
import io.github.jan.einkaufszettel.common.repositories.product.User
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.storage.storage
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: Int,
    @SerialName("created_at")
    val createdAt: Instant,
    val owner: User,
    @SerialName("image_path")
    val imagePath: String,
    val description: String,
    @SerialName("authorized_users")
    val authorizedUsers: List<User>?,
    val isOwner: Boolean = false,
) {

    @Composable
    fun Image(supabaseClient: SupabaseClient, modifier: Modifier = Modifier) {
        AsyncImage(imagePath, modifier) {
            supabaseClient.storage["cards"].download(imagePath)
        }
    }

}
