package com.dicoding.picodiploma.mycamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.mycamera.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.cameraXButton.setOnClickListener { startCameraX() }
    binding.cameraButton.setOnClickListener { startTakePhoto() }
    binding.galleryButton.setOnClickListener { startGallery() }
    binding.uploadButton.setOnClickListener { uploadImage() }

    // request permission saat activity dimulai
    if (!allPermissionGranted()) {
      ActivityCompat.requestPermissions(
          this,
          REQUIRED_PERMISSIONS,
          REQUEST_CODE_PERMISSIONS,
      )
    }
  }

  private fun uploadImage() {
    Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
  }

  private fun startGallery() {
    Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
  }

  private fun startTakePhoto() {
    Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
  }

  private fun startCameraX() {
    // beralih ke activiy camera
    val intent = Intent(this, CameraActivity::class.java)

    // membawa intent dan mengharapkan result
    launcherIntentCameraX.launch(intent)
  }

  private val launcherIntentCameraX =
  // mengharapkan result dari activity selanjutnya
  registerForActivityResult(
          ActivityResultContracts.StartActivityForResult(),
      ) {
        // kalau resultnya sesuai dengan kode yang diharapkan
        if (it.resultCode == CAMERA_X_RESULT) {
          // dapatkan data yang dihasilkan dalam extra
          // file yang dihasilkan dari camera
          val myFile = it.data?.getSerializableExtra(CameraActivity.EXTRA_PICTURE) as File

          // pakai kamera belakang atau depan
          val isBackCamera =
              it.data?.getBooleanExtra(CameraActivity.EXTRA_IS_BACK_CAMERA, true) as Boolean

          // pengolahan hasil dilakukan di Utils.kt
          val result =
              rotateBitmap(
                  BitmapFactory.decodeFile(myFile.path),
                  isBackCamera,
              )

          // hasil ditampilkan dalam komponen
          binding.previewImageView.setImageBitmap(result)
        }
      }

  // listener untuk hasil get permission
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      if (!allPermissionGranted()) {
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
        finish()
      }
    }
  }

  // cek apakah semua permission yang dibutuhkan sudah diijinkan
  private fun allPermissionGranted() =
      REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
      }

  companion object {
    const val CAMERA_X_RESULT = 200
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private const val REQUEST_CODE_PERMISSIONS = 10
  }
}
