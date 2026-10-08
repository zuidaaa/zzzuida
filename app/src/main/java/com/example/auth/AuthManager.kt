package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val auth = Firebase.auth

    fun attemptAutoSignIn(
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        if (auth.currentUser != null) {
            onSuccess()
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onFailure(e)
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                handleCredential(result.credential)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onFailure(e)
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                handleCredential(result.credential)
                onSuccess()
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthManager", "Cancelled", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("AuthManager", "Sign-in failed", e)
                onFailure(e)
            }
        }
    }

    // Exchange the Google ID token for a Firebase Credential
    fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: (com.google.firebase.auth.FirebaseUser?) -> Unit = {},
        onFailure: (Exception?) -> Unit = {}
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    onSuccess(user)
                } else {
                    onFailure(task.exception)
                }
            }
    }

    private suspend fun handleCredential(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(authCredential).await()
        } else {
            throw IllegalStateException("Unexpected credential type")
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthManager", "Clear state failed", e)
            } finally {
                onComplete()
            }
        }
    }
}
