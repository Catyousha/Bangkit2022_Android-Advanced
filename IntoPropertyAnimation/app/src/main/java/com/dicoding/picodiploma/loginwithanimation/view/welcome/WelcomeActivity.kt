package com.dicoding.picodiploma.loginwithanimation.view.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.animation.AppAnimation
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityWelcomeBinding
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginActivity
import com.dicoding.picodiploma.loginwithanimation.view.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {
  private lateinit var binding: ActivityWelcomeBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityWelcomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupView()
    setupAction()
    playAnimation()
  }

  private fun playAnimation() {

    AppAnimation.moveLeftRight(target = binding.imageView).start()

    val login = AppAnimation.fadeIn(target = binding.loginButton)
    val signup = AppAnimation.fadeIn(target = binding.signupButton)
    val title = AppAnimation.fadeIn(target = binding.titleTextView)
    val desc = AppAnimation.fadeIn(target = binding.descTextView)

    val together = AnimatorSet().apply {
      playTogether(login, signup)
    }

    AnimatorSet().apply {
      playSequentially(together, title, desc)
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

  private fun setupAction() {
    binding.loginButton.setOnClickListener {
      startActivity(Intent(this, LoginActivity::class.java))
    }

    binding.signupButton.setOnClickListener {
      startActivity(Intent(this, SignupActivity::class.java))
    }
  }
}