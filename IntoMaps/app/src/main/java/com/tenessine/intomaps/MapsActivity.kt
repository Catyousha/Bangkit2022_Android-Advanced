package com.tenessine.intomaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tenessine.intomaps.databinding.ActivityMapsBinding
import java.util.concurrent.TimeUnit

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

  private lateinit var binding: ActivityMapsBinding

  // objek sdk google maps
  private lateinit var mMap: GoogleMap

  // mendapatkan data lokasi user dari berbagai sumber
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  // parameter untuk dikirimkan ke fusedLocationClient
  private lateinit var locationRequest: LocationRequest

  // callback yang akan diaktifkan seiring lokasi user berubah
  private lateinit var locationCallback: LocationCallback

  private var isTracking = false

  // menampung koordinat yang dilalui user
  private var allLatLng = ArrayList<LatLng>()

  // mem-build cengkraman pada kamera agar selalu fokus ke marker
  private lateinit var boundsBuilder: LatLngBounds.Builder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMapsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    // inisialisasi fusedLocationClient
    fusedLocationClient = getFusedLocationProviderClient(this)

    if (!allPermissionGranted()) {
      ActivityCompat.requestPermissions(
          this,
          REQUIRED_PERMISSIONS,
          REQUEST_CODE_PERMISSIONS,
      )
    }
  }

  /**
   * Manipulates the map once available. This callback is triggered when the map is ready to be
   * used. This is where we can add markers or lines, add listeners or move the camera. In this
   * case, we just add a marker near Sydney, Australia. If Google Play services is not installed on
   * the device, the user will be prompted to install it inside the SupportMapFragment. This method
   * will only be triggered once the user has installed Google Play services and returned to the
   * app.
   */
  // dieksekusi setelah map di-mount
  @SuppressLint("MissingPermission")
  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap

    // Add a marker in Sydney and move the camera
    val dicodingSpace = LatLng(-6.8957643, 107.6338462)
    mMap.apply {

      // custom location marker
      addMarker(
          MarkerOptions()
              .position(dicodingSpace)
              .title("Dicoding Space")
              .snippet("Batik Kumeli No.50")
              .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)),
      )

      // konfigurasi UI map
      uiSettings.apply {
        if (allPermissionGranted()) {
          isMyLocationEnabled = true
        }
        isCompassEnabled = true
        isZoomControlsEnabled = true
        isMapToolbarEnabled = true
        isIndoorLevelPickerEnabled = true
      }
    }

    // mengirimkan request lokasi user
    createLocationRequest()

    // mendapatkan respons lokasi user
    createLocationCallback()
    setupBtnListener()
  }

  private fun setupBtnListener() {
    binding.btnStart.setOnClickListener {
      if (!isTracking) {
        clearMaps()
        updateTrackingStatus(true)
        startLocationUpdates()
      } else {
        updateTrackingStatus(false)
        stopLocationUpdates()
      }
    }
  }

  // deteksi lokasi secara real-time
  @SuppressLint("MissingPermission")
  private fun startLocationUpdates() {
    if (!allPermissionGranted()) {
      return
    }
    try {
      // colokkan request dan callback yang dibuat dalam fusedLocationClient
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper(),
      )
    } catch (e: Exception) {
      Log.d("MapActivity", "Error: ${e.message}")
    }
  }

  private fun stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }

  // membuat request lokasi untuk dikirimkan ke fusedLocationClient
  private fun createLocationRequest() {
    // konfigurasi request yang akan dikirimkan
    locationRequest =
        LocationRequest.create().apply {
          interval = TimeUnit.SECONDS.toMillis(1)
          maxWaitTime = TimeUnit.SECONDS.toMillis(1)
          priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    // build request lokasi
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    // buat client untuk mendapatkan lokasi
    val client = LocationServices.getSettingsClient(this)
    client
        // colok request lokasi
        .checkLocationSettings(builder.build())

        // colok success handling
        .addOnSuccessListener { getMyLastLocation() }

        // colok error handling
        .addOnFailureListener {
          // kalau exceptionnya bisa diselesaikan / resolvable, coba diselesaikan disini
          if (it is ResolvableApiException) {
            try {
              // kirim intentsenderrequest ke sistem untuk disolve
              // tata cara pen-solve-an udah dideklarasikan di getter resolution
              resolutionLauncher.launch(IntentSenderRequest.Builder(it.resolution).build())
            } catch (sendEx: IntentSender.SendIntentException) {
              Toast.makeText(this, sendEx.localizedMessage, Toast.LENGTH_SHORT).show()
            }
          }
        }
  }

  // untuk memastikan tidak ada setting lokasi yang error
  private val resolutionLauncher =
    registerForActivityResult(
      // pensolvean problem dilakukan disini
      ActivityResultContracts.StartIntentSenderForResult(),
    ) {
      when (it.resultCode) {
        RESULT_OK -> {
          Toast.makeText(this, "All location settings are satisfied", Toast.LENGTH_SHORT).show()
        }
        RESULT_CANCELED -> {
          Toast.makeText(this, "GPS must be activated!", Toast.LENGTH_SHORT).show()
        }
        else -> {
          Toast.makeText(this, "Failed with result code $it", Toast.LENGTH_SHORT).show()
        }
      }
    }

  // terima lokasi user
  private fun createLocationCallback() {
    locationCallback =
        object : LocationCallback() {
          // ketika lokasi berubah dan berhasil diambil
          override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {

              // tampung seluruh koordinat lokasi user
              val lastLatLng = LatLng(location.latitude, location.longitude)
              allLatLng.add(lastLatLng)

              // di map gambar garis yang nyambungin seluruh titik dalam allLatLng
              mMap.addPolyline(
                  PolylineOptions().addAll(allLatLng).width(10f).color(Color.RED),
              )

              // cengkram kamera dalam map agar selalu tetap di sekitar marker
              boundsBuilder.include(lastLatLng)
              val bounds: LatLngBounds = boundsBuilder.build()
              mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64))
            }
          }
        }
  }

  @SuppressLint("MissingPermission")
  private fun getMyLastLocation() {
    if (!allPermissionGranted()) return
    // dapatkan lokasi terakhir dari fusedLocationClient
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
      if (location != null) {
        // gambarkan marker lokasi user
        showMarker(
            location,
            "Start Point",
            null,
        )
      } else {
        Toast.makeText(
                this@MapsActivity,
                "Location is not found. Try Again",
                Toast.LENGTH_SHORT,
            )
            .show()
      }
    }
  }

  private fun clearMaps() {
    mMap.clear()
    allLatLng.clear()
    boundsBuilder = LatLngBounds.Builder()
  }

  private fun updateTrackingStatus(newStatus: Boolean) {
    isTracking = newStatus
    if (isTracking) {
      binding.btnStart.text = getString(R.string.stop_running)
    } else {
      binding.btnStart.text = getString(R.string.start_running)
    }
  }

  private fun showMarker(
    location: Location,
    title: String,
    snippet: String?,
  ) {
    val startLocation = LatLng(location.latitude, location.longitude)
    mMap.apply {
      addMarker(MarkerOptions().position(startLocation).title(title).snippet(snippet))
      moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.map_options, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.normal_type -> {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        true
      }
      R.id.satellite_type -> {
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        true
      }
      R.id.terrain_type -> {
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        true
      }
      R.id.hybrid_type -> {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        true
      }
      else -> {
        super.onOptionsItemSelected(item)
      }
    }
  }

  private fun allPermissionGranted(): Boolean {
    val permissionGranted =
        REQUIRED_PERMISSIONS.all {
          ActivityCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    if (!permissionGranted) {
      Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }
    return permissionGranted
  }

  override fun onResume() {
    super.onResume()
    startLocationUpdates()
  }

  override fun onPause() {
    super.onPause()
    stopLocationUpdates()
  }

  companion object {
    private const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
  }
}
