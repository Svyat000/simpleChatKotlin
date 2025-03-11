package com.sddrozdov.simplechatkotlin.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.sddrozdov.simplechatkotlin.accountHelper.GoogleSignInAccountHelper
import com.sddrozdov.simplechatkotlin.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private var _binding: ActivitySignInBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private val googleSignInAccountHelper = GoogleSignInAccountHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetUtil.applyWindowInsets(binding.root)

        binding.googleSignIn.setOnClickListener{
            googleSignInAccountHelper.launchCredentialManager()
        }
    }

    fun uiUpdate(user: FirebaseUser?) {
    }
}