package com.tenessine.intocanvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat

class MainActivity : AppCompatActivity() {
  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val mBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    val mCanvas = Canvas(mBitmap)
    val mPaint = Paint()

    val mRect = Rect()
    mRect.set(mBitmap.width / 2 - 100, mBitmap.height / 2 - 100, mBitmap.width / 2 + 100, mBitmap.height / 2 + 100)

    val mBounds = Rect()
    val mPaintText = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
    val text = "Hello World"
    mPaintText.apply {
      textSize = 20f
      color = ResourcesCompat.getColor(resources, R.color.white, null)
      getTextBounds(text, 0, text.length, mBounds)
    }

    val xText: Int = mBitmap.width / 2 - mBounds.centerX()
    val yText: Int = mBitmap.height / 2 - mBounds.centerY()

    val myImageView = findViewById<ImageView>(R.id.myImageView)
    myImageView.setImageBitmap(mBitmap)

    mCanvas.apply{
      drawColor(ResourcesCompat.getColor(resources, R.color.teal_200, null))
      save()

      mPaint.color = ResourcesCompat.getColor(resources, R.color.black, null)
      clipOutRect(mRect)

      mPaint.color = ResourcesCompat.getColor(resources, R.color.purple_200, null)
      drawCircle((mBitmap.width/2).toFloat(), (mBitmap.height/2).toFloat(), 200f, mPaint)

      restore()

      drawText(text, xText.toFloat(), yText.toFloat(), mPaintText)

    }

  }
}