package com.tenessine.advancedcustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.tenessine.advancedcustomview.view.SeatsView

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    supportActionBar?.hide()

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val seatsView = findViewById<SeatsView>(R.id.seatsView)
    val button = findViewById<Button>(R.id.finishButton)

    button.setOnClickListener {
      seatsView.seat?.let {
        Toast.makeText(this, "Selected seat: ${it.name}", Toast.LENGTH_SHORT).show()
      } ?: run {
        Toast.makeText(this, "Please select a seat.", Toast.LENGTH_SHORT).show()
      }
    }
  }
}