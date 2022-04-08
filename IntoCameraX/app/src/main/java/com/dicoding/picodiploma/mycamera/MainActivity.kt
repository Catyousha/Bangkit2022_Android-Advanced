package com.dicoding.picodiploma.mycamera

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.picodiploma.mycamera.databinding.ActivityMainBinding
import com.dicoding.picodiploma.mycamera.service.ApiConfig
import com.dicoding.picodiploma.mycamera.service.FileUploadResponse
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var currentPhotoPath: String
  private var getFile: File? = null

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
    if (getFile != null) {
      val file = reduceFileImage(getFile as File)
      val description = "Awanama was here".toRequestBody("text/plain".toMediaType())
      val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
      val imageMultipart: MultipartBody.Part =
          MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

      val service = ApiConfig().getApiService().uploadImage(imageMultipart, description)
      service.enqueue(
          object : Callback<FileUploadResponse> {
            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
              if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && !responseBody.error) {
                  Toast.makeText(this@MainActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                }
              } else {
                Toast.makeText(this@MainActivity, response.message(), Toast.LENGTH_SHORT).show()
              }
            }
            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
              Toast.makeText(this@MainActivity, "Gagal instance Retrofit", Toast.LENGTH_SHORT)
                  .show()
            }
          })
    } else {
      Toast.makeText(this, "No file choosen", Toast.LENGTH_SHORT).show()
    }
  }

  private fun startGallery() {
    // membuat intent untuk membuka galeri
    // menentukan aksi dan tipe file yang akan dibuka
    val intent =
        Intent().apply {
          action = ACTION_GET_CONTENT
          type = "image/*"
        }

    // membuat chooser untuk memilih file dengan intent yang telah dibuat
    val chooser = Intent.createChooser(intent, "Choose image")

    // memulai activity untuk memilih file
    launcherIntentGallery.launch(chooser)
  }

  private fun startTakePhoto() {
    // intent camera menggunakan fitur kamera yang sudah ada
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    // kemukakan activity apa yang memerintahkan intent ini
    intent.resolveActivity(packageManager)

    // membuat temp file untuk menyimpan gambar yang dicapture
    createTempFile(application).also {
      val photoURI: Uri =
          FileProvider.getUriForFile(
              this@MainActivity,
              packageName,
              it,
          )
      currentPhotoPath = it.absolutePath

      // menambahkan file yang telah dibuat ke intent
      intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

      // PROSES CAPTURE GAMBAR BERLANGSUNG DISINI
      launcherIntentCamera.launch(intent)
    }
  }

  private fun startCameraX() {
    // cameraX membuat fitur kamera baru
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
          getFile = myFile

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

  private val launcherIntentCamera =
      registerForActivityResult(
          ActivityResultContracts.StartActivityForResult(),
      ) {
        if (it.resultCode == RESULT_OK) {
          // dapatkan path yang ada
          val myFile = File(currentPhotoPath)
          getFile = myFile

          // mengolah bitmap dari path
          val result = BitmapFactory.decodeFile(myFile.path)

          // tampilkan preview
          binding.previewImageView.setImageBitmap(result)
        }
      }

  private val launcherIntentGallery =
      registerForActivityResult(
          ActivityResultContracts.StartActivityForResult(),
      ) {
        if (it.resultCode == RESULT_OK) {
          // file yang dipilih didapatkan sebagai Uri
          val selectedImg: Uri = it.data?.data as Uri

          // set preview
          binding.previewImageView.setImageURI(selectedImg)

          // ubah uri menjadi file dengan bantuan Utils.kt
          val myFile = uriToFile(selectedImg, this@MainActivity)
          getFile = myFile
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
    const val EXTRA_DATA = "data"
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private const val REQUEST_CODE_PERMISSIONS = 10
  }
}
