package com.dicoding.picodiploma.loginwithanimation.animation

import android.animation.ObjectAnimator
import android.view.View

object AppAnimation {

  fun moveLeftRight(
    target: View,
    moveLeft: Float = -30.0f,
    moveRight: Float = 30.0f,
    duration: Long = 6000
  ): ObjectAnimator {
    return ObjectAnimator.ofFloat(target, View.TRANSLATION_X, moveLeft, moveRight).apply {
      this.duration = duration
      this.repeatCount = ObjectAnimator.INFINITE
      this.repeatMode = ObjectAnimator.REVERSE
    }
  }

  fun fadeIn(target: View, duration: Long = 500): ObjectAnimator {
    return ObjectAnimator.ofFloat(target, View.ALPHA, 1f).setDuration(duration)
  }
}