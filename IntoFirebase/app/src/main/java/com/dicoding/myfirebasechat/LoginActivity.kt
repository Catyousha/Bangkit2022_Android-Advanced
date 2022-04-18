package com.dicoding.myfirebasechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.myfirebasechat.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private lateinit var googleSignInClient: GoogleSignInClient
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val gso = GoogleSignInOptions
      .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken("495219729472-gk8qp45b7i2vnulk1bph7kimcpkb0ebu.apps.googleusercontent.com")
      .requestEmail()
      .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso)
    auth = Firebase.auth

    binding.signInButton.setOnClickListener {
      signIn()
    }
  }

  private fun signIn() {
    val signInIntent = googleSignInClient.signInIntent
    resultLauncher.launch(signInIntent)
  }

  private var resultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) {
    if (it.resultCode == Activity.RESULT_OK) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
      try {
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account?.idToken ?: "")
      } catch (e: ApiException) {
        e.printStackTrace()
      }
    }
  }

  private fun firebaseAuthWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
      .addOnCompleteListener(this) {
        if (it.isSuccessful) {
          val user = auth.currentUser
          updateUI(user)
        } else {
          updateUI(null)
        }
      }
  }

  private fun updateUI(currentUser: FirebaseUser?) {
    if (currentUser == null) return
    val intent = Intent(this@LoginActivity, MainActivity::class.java)
    startActivity(intent)
    finish()
  }
}