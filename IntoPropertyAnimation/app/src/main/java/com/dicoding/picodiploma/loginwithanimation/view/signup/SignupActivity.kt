package com.dicoding.picodiploma.loginwithanimation.view.signup

import android.animation.AnimatorSet
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.loginwithanimation.animation.AppAnimation
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySignupBinding
import com.dicoding.picodiploma.loginwithanimation.model.UserModel
import com.dicoding.picodiploma.loginwithanimation.model.UserPreference
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import kotlinx.coroutines.delay

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignupActivity : AppCompatActivity() {
  private lateinit var binding: ActivitySignupBinding
  private lateinit var signupViewModel: SignupViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignupBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupView()
    setupViewModel()
    setupAction()
    playAnimation()
  }

  private fun playAnimation() {
    AppAnimation.moveLeftRight(binding.imageView).start()

    val titleAnim = AppAnimation.fadeIn(binding.titleTextView)

    val nameAnim = AppAnimation.fadeIn(binding.nameTextView)
    val nameLayoutAnim = AppAnimation.fadeIn(binding.nameEditTextLayout)
    val nameAnimTogether = AnimatorSet().apply {
      playTogether(nameAnim, nameLayoutAnim)
    }

    val emailAnim = AppAnimation.fadeIn(binding.emailTextView)
    val emailLayoutAnim = AppAnimation.fadeIn(binding.emailEditTextLayout)
    val emailAnimTogether = AnimatorSet().apply {
      playTogether(emailAnim, emailLayoutAnim)
    }

    val passwordAnim = AppAnimation.fadeIn(binding.passwordTextView)
    val passwordLayoutAnim = AppAnimation.fadeIn(binding.passwordEditTextLayout)
    val passwordAnimTogether = AnimatorSet().apply {
      playTogether(passwordAnim, passwordLayoutAnim)
    }

    val signupAnim = AppAnimation.fadeIn(binding.signupButton)

    AnimatorSet().apply {
      playSequentially(
        titleAnim,
        nameAnimTogether,
        emailAnimTogether,
        passwordAnimTogether,
        signupAnim
      )
    }.start()


  }

  private fun setupView() {
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
    supportActionBar?.hide()
  }

  private fun setupViewModel() {
    signupViewModel = ViewModelProvider(
      this,
      ViewModelFactory(UserPreference.getInstance(dataStore))
    )[SignupViewModel::class.java]
  }

  private fun setupAction() {
    binding.signupButton.setOnClickListener {
      val name = binding.nameEditText.text.toString()
      val email = binding.emailEditText.text.toString()
      val password = binding.passwordEditText.text.toString()
      when {
        name.isEmpty() -> {
          binding.nameEditTextLayout.error = "Masukkan email"
        }
        email.isEmpty() -> {
          binding.emailEditTextLayout.error = "Masukkan email"
        }
        password.isEmpty() -> {
          binding.passwordEditTextLayout.error = "Masukkan password"
        }
        else -> {
          signupViewModel.saveUser(UserModel(name, email, password, false))
          AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage("Akunnya sudah jadi nih. Yuk, login dan belajar coding.")
            setPositiveButton("Lanjut") { _, _ ->
              finish()
            }
            create()
            show()
          }
        }
      }
    }
  }
}