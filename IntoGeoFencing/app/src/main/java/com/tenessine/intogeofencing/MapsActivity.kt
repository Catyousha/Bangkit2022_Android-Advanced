package com.tenessine.intogeofencing

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tenessine.intogeofencing.databinding.ActivityMapsBinding

@SuppressLint("UnspecifiedImmutableFlag")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var mMap: GoogleMap
  private lateinit var binding: ActivityMapsBinding
  private lateinit var geofencingClient: GeofencingClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMapsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager
      .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    if (!allPermissionGranted()) {
      ActivityCompat.requestPermissions(
        this,
        REQUIRED_PERMISSIONS,
        REQUEST_CODE_PERMISSIONS,
      )
    }
  }

  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @SuppressLint("MissingPermission")
  override fun onMapReady(googleMap: GoogleMap) {
    // setup marker awal sebagai lokasi geofencing
    val standford = LatLng(CENTER_LAT, CENTER_LNG)
    mMap = googleMap
    mMap.apply {
      addMarker(
        MarkerOptions()
          .position(standford)
          .title("Standford University")
          .snippet("Standford, California, USA")
      )
      moveCamera(CameraUpdateFactory.newLatLngZoom(standford, 15f))
      addCircle(
        CircleOptions()
          .center(standford)
          .radius(GEOFENCE_RADIUS)
          .strokeColor(Color.RED)
          .fillColor(0x22FF0000)
          .strokeWidth(5f)
      )

      // konfigurasi ui gmaps
      uiSettings.apply {
        if (allPermissionGranted()) {
          isMyLocationEnabled = true
        }
        isCompassEnabled = true
        isZoomControlsEnabled = true
        isMapToolbarEnabled = true
        isIndoorLevelPickerEnabled = true
      }

      addGeofence()
    }
  }

  @SuppressLint("MissingPermission")
  private fun addGeofence() {
    // buat client untuk melakukan geofencing
    geofencingClient = LocationServices.getGeofencingClient(this)

    // build geofencing request
    val geofence = Geofence.Builder()
      // id
      .setRequestId("University")

      // area lokasi
      .setCircularRegion(CENTER_LAT, CENTER_LNG, GEOFENCE_RADIUS.toFloat())

      // kadaluarsa geofence
      .setExpirationDuration(Geofence.NEVER_EXPIRE)

      // transisi yang diterima oleh geofencing yang akan diproses
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

        // delay antara ENTER dan DWELL, masuk dan bisa dikatakan tinggal
      .setLoiteringDelay(5000)
      .build()

    // 'compile' request yang dibuat
    val geofencingRequest = GeofencingRequest.Builder()
        // pemicu awal (ENTER)
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // colok konfigurasi request
      .addGeofence(geofence)
      .build()

    // hapus geofencing sebelumnya yang terasosiasi dengan pendingintent berikut
    geofencingClient.removeGeofences(geofencePendingIntent).run {
      addOnCompleteListener {
        // setelah diremove, entah emang ada sebelumnya atau kagak, tambahkan geofencing baru
        // kaitkan request dengan pendingintent
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
          addOnSuccessListener {
            Log.d(TAG, "Geofence added")
          }
          addOnFailureListener {
            Log.d(TAG, "Geofence failed")
          }
        }

      }
    }
  }

  // yang akan dipanggil ketika transisi geofencing terjadi
  private val geofencePendingIntent: PendingIntent by lazy {
    // membuat intent untuk menampung data action dan tujuan broadcast receivernya
    val intent = Intent(this, GeofenceBroadcastReceiver::class.java).apply {
      action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
    }

    // eksekusi broadcast
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    } else {
      PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
  }

  private fun allPermissionGranted(): Boolean {
    val permissionGranted =
      REQUIRED_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
      }
    if (!permissionGranted) {
      Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
    }
    return permissionGranted
  }

  companion object {
    private const val TAG = "GeofenceActivity"
    private const val CENTER_LAT = 37.4274745
    private const val CENTER_LNG = -122.169719
    private const val GEOFENCE_RADIUS = 400.0

    private const val REQUEST_CODE_PERMISSIONS = 10

    @RequiresApi(Build.VERSION_CODES.Q)
    val REQUIRED_PERMISSIONS =
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      )
  }
}