package com.catedra.bitacora.features.auth.presentation.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun createGoogleSignInHandler(
    context: Context,
    coroutineScope: CoroutineScope,
    viewModel: AuthViewModel
): () -> Unit {
    val credentialManager = CredentialManager.create(context)
    
    return {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("1901113908-r6sliik0sosrd0a7p9n7v1o11bih36pm.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                Log.d("Auth", "Iniciando petición de Credential Manager...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.w("Auth", "Tipo de credencial no esperado: ${credential.type}")
                }
            } catch (e: GetCredentialException) {
                Log.e("Auth", "Error de Credential Manager: ${e.message}")
            } catch (e: Exception) {
                Log.e("Auth", "Error inesperado en flujo de Google: ${e.message}", e)
            }
        }
    }
}
