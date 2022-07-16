package io.github.jan.einkaufszettel.common.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.jan.einkaufszettel.common.EMAIL_REGEX
import io.github.jan.einkaufszettel.common.components.GoogleButton
import io.github.jan.einkaufszettel.common.components.PasswordField
import io.github.jan.einkaufszettel.common.controller.AuthController
import io.github.jan.supacompose.CurrentPlatformTarget
import io.github.jan.supacompose.PlatformTarget

@Composable
fun AuthScreen(authController: AuthController) {
    var signup by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loggingIn by remember { mutableStateOf<String?>(null) }
    var forgotPassword by remember { mutableStateOf<Boolean>(false) }
    if (loggingIn == null) {
        Box(contentAlignment = Alignment.Center) {
            Column {
                TextField(
                    email,
                    {
                        email = it;
                        emailError = if (!EMAIL_REGEX.matches(it)) {
                            "Keine gültige E-Mail"
                        } else {
                            null
                        }
                    },
                    label = { Text("E-Mail") },
                    singleLine = true,
                    isError = emailError != null,
                    placeholder = { Text("name@gmail.com") })
                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                PasswordField(password, {
                    password = it
                    passwordError = if (it.length < 6) {
                        "Passwort muss mindestens 6 Zeichen lang sein"
                    } else {
                        null
                    }
                }, label = "Password")
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                val buttonText = if (signup) "Registrieren" else "Einloggen"
                Button(
                    {
                        if (signup) {
                            loggingIn = "Registriere..."
                            authController.signUpWithEmail(email, password) {
                                loggingIn = null
                            }
                        } else {
                            loggingIn = "Melde an..."
                            authController.loginWithEmail(email, password) {
                                loggingIn = null
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = passwordError == null && emailError == null && email.isNotBlank() && password.isNotBlank()
                ) {
                    Text(buttonText)
                }
                GoogleButton(Modifier.padding(12.dp).align(Alignment.CenterHorizontally), icon = { Icon(getGoogleLogo(), "", tint = Color.Unspecified) }, onClicked = { authController.loginWithGoogle() }, text = "Mit Google anmelden", loadingText = "Öffne Browser...")
                if(CurrentPlatformTarget == PlatformTarget.ANDROID) {
                    Button({
                        forgotPassword = true
                    }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Password vergessen?")
                    }
                }
            }
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
                Column {
                    Text(if (signup) "Bereits registriert?" else "Noch kein Account erstellt?")
                    Button(
                        onClick = { signup = !signup },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(if (signup) "Einloggen" else "Jetzt erstellen")
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(loggingIn!!, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
    if(forgotPassword && CurrentPlatformTarget == PlatformTarget.ANDROID) {
        PasswordRecoverDialog({ forgotPassword = false }) {
            authController.sendPasswordRecovery(it)
        }
    }
}

@Composable
expect fun PasswordRecoverDialog(disable: () -> Unit, sendPasswordRecovery: (String) -> Unit)

@Composable
expect fun getGoogleLogo(): Painter