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
    geofencingClient = LocationServices.getGeofencingClient(this)
    val geofence = Geofence.Builder()
      .setRequestId("University")
      .setCircularRegion(CENTER_LAT, CENTER_LNG, GEOFENCE_RADIUS.toFloat())
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
      .setLoiteringDelay(5000)
      .build()

    val geofencingRequest = GeofencingRequest.Builder()
      .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
      .addGeofence(geofence)
      .build()

    geofencingClient.removeGeofences(geofencePendingIntent).run {
      addOnCompleteListener {
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

  private val geofencePendingIntent: PendingIntent by lazy {
    val intent = Intent(this, GeofenceBroadcastReceiver::class.java).apply {
      action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
    }
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