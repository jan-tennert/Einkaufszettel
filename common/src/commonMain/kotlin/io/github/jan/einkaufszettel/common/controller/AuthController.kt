package io.github.jan.einkaufszettel.common.controller

interface AuthController {

    fun signUpWithEmail(email: String, password: String, callback: () -> Unit)

    fun loginWithEmail(email: String, password: String, callback: () -> Unit)

    fun loginWithGoogle()

    fun sendPasswordRecovery(email: String)

}