package io.github.jan.einkaufszettel.common.ui.dialog

import androidx.compose.runtime.Composable
import io.github.jan.supacompose.auth.user.UserSession

@Composable
expect fun ProfileDialog(session: UserSession, createProfile: (name: String) -> Unit, logout: () -> Unit, disable: () -> Unit)