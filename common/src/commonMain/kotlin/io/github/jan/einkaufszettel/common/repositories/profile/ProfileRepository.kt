package io.github.jan.einkaufszettel.common.repositories

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(val id: String = "", val username: String)

sealed interface UserStatus {

    data class Success(val profile: UserProfile) : UserStatus

    object NotFound : UserStatus

    object Loading : UserStatus

    object Error : UserStatus

    object NotTried : UserStatus

}

sealed interface ProfileRepository {

    suspend fun getProfile(id: String) : UserProfile?

    suspend fun createProfile(id: String, username: String): UserProfile

    suspend fun updateUsername(id: String, username: String)

}

class ProfileRepositoryImpl(private val supabaseClient: SupabaseClient) : ProfileRepository {

    override suspend fun getProfile(id: String): UserProfile? = supabaseClient.postgrest["profiles"].select {
        UserProfile::id eq id
    }.decodeAs<List<UserProfile>>().firstOrNull()

    override suspend fun createProfile(id: String, username: String) = supabaseClient.postgrest["profiles"].insert(
        UserProfile(id, username)
    ).decodeAs<List<UserProfile>>().first()

    override suspend fun updateUsername(id: String, username: String) {
        supabaseClient.postgrest["profiles"].update({
            UserProfile::username setTo username
        }) {
            UserProfile::id eq id
        }
    }

}