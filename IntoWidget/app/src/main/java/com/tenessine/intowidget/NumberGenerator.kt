package com.tenessine.intowidget

internal object NumberGenerator {
  fun geterate(max: Int): Int {
    return (Math.random() * max).toInt()
  }
}