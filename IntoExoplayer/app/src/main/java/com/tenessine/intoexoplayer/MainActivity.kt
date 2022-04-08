package com.tenessine.intoexoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.tenessine.intoexoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
    ActivityMainBinding.inflate(layoutInflater)
  }
  private var player: ExoPlayer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(viewBinding.root)


  }

  private fun initializePlayer() {
    val mediaItem = MediaItem.fromUri(URL_VIDEO_DICODING)
    val anotherMediaItem = MediaItem.fromUri(URL_AUDIO)

    player = ExoPlayer.Builder(this).build().also { exoPlayer ->
      viewBinding.videoView.player = exoPlayer.apply {
        setMediaItem(mediaItem)
        addMediaItem(anotherMediaItem)
        prepare()
      }
    }
  }

  private fun releasePlayer() {
    player?.release()
    player = null
  }

  private fun hideSystemUI(){
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, viewBinding.videoView).let {
      it.apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    }
  }

  // responsive issues
  override fun onStart() {
    super.onStart()
    initializePlayer()
  }

  override fun onResume() {
    super.onResume()
    initializePlayer()
    hideSystemUI()
  }

  override fun onPause() {
    super.onPause()
    releasePlayer()
  }

  override fun onStop() {
    super.onStop()
    releasePlayer()
  }

  companion object {
    const val URL_VIDEO_DICODING =
      "https://github.com/dicodingacademy/assets/releases/download/release-video/VideoDicoding.mp4"
    const val URL_AUDIO =
      "https://github.com/dicodingacademy/assets/raw/main/android_intermediate_academy/bensound_ukulele.mp3"
  }
}