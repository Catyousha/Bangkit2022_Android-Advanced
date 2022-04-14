package com.tenessine.intogeofencing

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

  @SuppressLint("LongLogTag")
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      // action yang diterima dari activity
      ACTION_GEOFENCE_EVENT -> {
        // menangkap pesan yang dibawa dari activity
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // error handling
        if (geofencingEvent.hasError()) {
          val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
          Log.e(TAG, errorMessage)
          return
        }

        // mengambil tipe transisi yang terjadi
        val geofenceTransition = geofencingEvent.geofenceTransition

        // kalau user masuk atau menetap dalam area geofencing, jalankan operasi
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

          // jenis pesan yang akan ditampilkan
          val geofenceTransitionString =
            when (geofenceTransition) {
              Geofence.GEOFENCE_TRANSITION_ENTER -> "Anda telah memasuki area"
              Geofence.GEOFENCE_TRANSITION_DWELL -> "Anda telah di dalam area"
              else -> "Invalid transition type"
            }

          // string yang bakal ditampilkan di notifikasi
          val triggeringGeofences = geofencingEvent.triggeringGeofences
          val requestId = triggeringGeofences[0].requestId

          val geofenceTransitionDetails =
            String.format("%s: %s", geofenceTransitionString, requestId)
          Log.i(TAG, geofenceTransitionDetails)

          sendNotification(context, geofenceTransitionDetails)
        } else {
          val errorMsg = "Geofence transition error: $geofenceTransition"
          Log.e(TAG, errorMsg)

          sendNotification(context, errorMsg)
        }
      }
    }
  }

  // notif yang akan muncul ketika geofence dalam kondisi yang ditentukan
  private fun sendNotification(context: Context, geofenceTransitionDetails: String) {
    val mNotificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setContentTitle(geofenceTransitionDetails)
      .setContentText("Anda sudah bisa absen sekarang :)")
      .setSmallIcon(R.drawable.ic_circle_notifications)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
      mBuilder.setChannelId(CHANNEL_ID)
      mNotificationManager.createNotificationChannel(channel)
    }
    val notification = mBuilder.build()
    mNotificationManager.notify(NOTIFICATION_ID, notification)
  }

  companion object {
    const val TAG = "GeofenceBroadcastReceiver"
    const val ACTION_GEOFENCE_EVENT = "com.tenessine.intogeofencing.ACTION_GEOFENCE_EVENT"
    private const val CHANNEL_ID = "1"
    private const val CHANNEL_NAME = "Geofence Channel"
    private const val NOTIFICATION_ID = 1
  }
}