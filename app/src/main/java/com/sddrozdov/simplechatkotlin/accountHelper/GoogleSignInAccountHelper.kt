package com.sddrozdov.simplechatkotlin.accountHelper

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sddrozdov.simplechatkotlin.activity.MainActivity
import com.sddrozdov.simplechatkotlin.R
import com.sddrozdov.simplechatkotlin.activity.SignInActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSignInAccountHelper(private val signInActivity: SignInActivity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(signInActivity)

    fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(signInActivity.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        signInActivity.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = signInActivity,
                    request = request
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("SvyatTAG", "Credential retrieval failed", e)
                when (e) {
                    is NoCredentialException -> {
                        Log.d("SvyatTAG", "No saved credentials found")
                    }
                    else -> {
                        Toast.makeText(signInActivity, "Sign-in error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w("SvyatTAG", "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        idToken?.let {
            val credential = GoogleAuthProvider.getCredential(it, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(signInActivity) { task ->
                    if (task.isSuccessful) {
                        Log.d("SvyatTAG", "GoogleSignInWithCredential:success")
                        signInActivity.uiUpdate(auth.currentUser)
                        checkAuthStatus()
                    } else {
                        Log.w("SvyatTAG", "GoogleSignInWithCredential:failure", task.exception)
                        signInActivity.uiUpdate(null)
                    }
                }
        } ?: run {
            Log.w("SvyatTAG", "ID Token is null")
            signInActivity.uiUpdate(null)
        }
    }
    object AuthManager {
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()

        fun signOut(context: Context) {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: ClearCredentialException) {
                    Log.e("AuthManager", "Clear credentials failed", e)
                }
            }
        }
    }
    private fun checkAuthStatus(){
        if(auth.currentUser != null){
            val i = Intent(signInActivity, MainActivity::class.java)
            signInActivity.startActivity(i)
            signInActivity.finish()
        }
    }
}
