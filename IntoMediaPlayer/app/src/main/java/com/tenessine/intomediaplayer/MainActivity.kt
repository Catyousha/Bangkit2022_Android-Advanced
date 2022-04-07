package com.tenessine.intomediaplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tenessine.intomediaplayer.service.MediaService

class MainActivity : AppCompatActivity() {

  // menerima pesan untuk dikirimkan ke service
  private var mService: Messenger? = null

  // menentukan aksi service yang digunakan (CREATE atau DESTROY)
  private lateinit var mBoundServiceIntent: Intent

  // menandakan apakah service sedang di-bind
  private var mServiceBound = false

  // menyimpan koneksi ke service
  private val mServiceConnection =
      object : ServiceConnection {

        // ketika terhubung ke service
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
          mService = Messenger(service)
          mServiceBound = true
        }

        // ketika service terputus
        override fun onServiceDisconnected(name: ComponentName?) {
          mService = null
          mServiceBound = false
        }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val btnPlay = findViewById<Button>(R.id.btn_play)
    val btnStop = findViewById<Button>(R.id.btn_stop)

    // menentukan service yang digunakan memiliki aksi ACTION_CREATE
    mBoundServiceIntent =
        Intent(this@MainActivity, MediaService::class.java).apply {
          action = MediaService.ACTION_CREATE
        }

    // menjalankan service, memicu fungsi onStartCommand()
    startService(mBoundServiceIntent)

    // menghubungkan ke service dengan menggunakan koneksi
    bindService(mBoundServiceIntent, mServiceConnection, BIND_AUTO_CREATE)

    btnPlay.setOnClickListener {
      if(mServiceBound) {
        try {
          // mengirimkan pesan ke service yaitu PLAY
          mService?.send(Message.obtain(null, MediaService.PLAY, 0, 0))
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    btnStop.setOnClickListener {
      if(mServiceBound) {
        try {
          // mengirimkan pesan ke service yaitu STOP
          mService?.send(Message.obtain(null, MediaService.STOP, 0, 0))
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    // unbind dan menghancurkan service
    unbindService(mServiceConnection)
    mBoundServiceIntent.action = MediaService.ACTION_DESTROY
    startService(mBoundServiceIntent)
  }
}
