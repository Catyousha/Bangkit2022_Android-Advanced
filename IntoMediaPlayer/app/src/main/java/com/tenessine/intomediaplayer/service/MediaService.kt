package com.tenessine.intomediaplayer.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import com.tenessine.intomediaplayer.R
import com.tenessine.intomediaplayer.callback.MediaPlayerCallback
import java.lang.ref.WeakReference

class MediaService : Service(), MediaPlayerCallback {
  private var mMediaPlayer: MediaPlayer? = null
  private var isReady: Boolean = false

  // berfungsi untuk mengatur callback kepada service ini
  private val mMessenger = Messenger(IncomingHandler(this))

  internal class IncomingHandler(
      playerCallback: MediaPlayerCallback,
  ) : Handler(Looper.getMainLooper()) {

    // weak reference untuk menghindari memory leak
    // untuk mereferensikan ke callback media player yang ada di service
    private val mediaPlayerCallbackWeakReference: WeakReference<MediaPlayerCallback> =
        WeakReference(playerCallback)

    // menerima pesan dari client
    // dan mengeksekusi perintah yang diberikan
    override fun handleMessage(msg: Message) {
      when (msg.what) {
        PLAY -> {
          mediaPlayerCallbackWeakReference.get()?.onPlay()
        }
        STOP -> {
          mediaPlayerCallbackWeakReference.get()?.onStop()
        }
        else -> super.handleMessage(msg)
      }
    }
  }

  // mengembalikan messenger yang akan digunakan oleh client
  override fun onBind(intent: Intent): IBinder {
    return mMessenger.binder
  }

  // dijalankan ketika service dimulai ( startService(intent) )
  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int,
  ): Int {
    when (intent?.action) {
      ACTION_CREATE ->
          if (mMediaPlayer == null) {
            init()
          }
      ACTION_DESTROY ->
          if (mMediaPlayer?.isPlaying == true) {
            stopSelf()
          }
      else -> {
        init()
      }
    }
    return flags
  }

  private fun init() {
    // prepare objek mediaplayer
    mMediaPlayer = MediaPlayer()

    // set atribut audio
    val attribute =
        AudioAttributes.Builder()
            // tipe audio, apakah media, alarm, atau bunyi notif
            .setUsage(AudioAttributes.USAGE_MEDIA)

            // tipe konten audio, apakah sound effect (sonification), musik, movie, atau speeck
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    // colokkan atribut
    mMediaPlayer?.setAudioAttributes(attribute)

    // dapatkan sumber audio mentah
    val afd = applicationContext.resources.openRawResourceFd(R.raw.raw_guitar)
    try {
      // colokkan sumber audio mentah yang udah di deskripsikan
      mMediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // yang terjadi kalau mediaplayer udah disiapkan
    mMediaPlayer?.setOnPreparedListener {
      isReady = true
      mMediaPlayer?.start()
    }

    // yang terjadi kalau mediaplayer error
    mMediaPlayer?.setOnErrorListener { _, _, _ -> false }
  }

  override fun onPlay() {
    // siapkan media player
    if (!isReady) {
      mMediaPlayer?.prepareAsync()
    }

    // jalankan media player
    else {
      if (mMediaPlayer?.isPlaying == true) {
        mMediaPlayer?.pause()
      } else {
        mMediaPlayer?.start()
      }
    }
  }

  override fun onStop() {
    // hentikan media player sekaligus unprepare.
    if (mMediaPlayer?.isPlaying as Boolean || isReady) {
      mMediaPlayer?.stop()
      isReady = false
    }
  }

  companion object {
    const val ACTION_CREATE = "com.tenessine.intomediaplayer.service.MediaService.ACTION_CREATE"
    const val ACTION_DESTROY = "com.tenessine.intomediaplayer.service.MediaService.ACTION_PLAY"
    const val PLAY = 0
    const val STOP = 1
  }
}
