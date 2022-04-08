package com.dicoding.picodiploma.mycamera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.mycamera.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
  private lateinit var binding: ActivityCameraBinding

  // gambar yang ditangkap oleh kamera
  private var imageCapture: ImageCapture? = null

  // pakai kamera yang mana
  private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityCameraBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.captureImage.setOnClickListener { takePhoto() }
    binding.switchCamera.setOnClickListener {
      // toggle perubahan kamera
      cameraSelector =
          if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
              CameraSelector.DEFAULT_FRONT_CAMERA
          else CameraSelector.DEFAULT_BACK_CAMERA
      startCamera()
    }
  }

  // dipanggil pas pertama kali activity dijalankan dan diresume
  public override fun onResume() {
    super.onResume()
    hideSystemUI()
    startCamera()
  }

  private fun takePhoto() {
    // kalau ga ada gambar yang ditangkap, maka akan kembali
    val imageCapture = imageCapture ?: return

    // menjadikan gambar tangkapan sebagai file
    val photoFile = createFile(application)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // jepret
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(this),
        object : ImageCapture.OnImageSavedCallback {
          override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

            // kembali ke activity sebelumnya dengan result berupa data gambar dsb dalam extra
            val intent =
                Intent().apply {
                  putExtra(EXTRA_PICTURE, photoFile)
                  putExtra(
                      EXTRA_IS_BACK_CAMERA,
                      cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA,
                  )
                }
            setResult(MainActivity.CAMERA_X_RESULT, intent)

            Toast.makeText(
                    this@CameraActivity,
                    "Photo capture succeeded: ${photoFile.absolutePath}",
                    Toast.LENGTH_SHORT)
                .show()
            finish()
          }

          override fun onError(exception: ImageCaptureException) {
            Toast.makeText(this@CameraActivity, "Error.", Toast.LENGTH_SHORT).show()
          }
        })
  }

  private fun startCamera() {
    // membuat camera provider, yang ngurusin pemrosesan kamera dan lifecyclenya
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    cameraProviderFuture.addListener(
        {
          val cameraProvider = cameraProviderFuture.get()
          val preview =
              Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
              }
          imageCapture = ImageCapture.Builder().build()

          try {
            // membuat tampilan camera dan mengikatnya ke lifecycleowner
            cameraProvider.apply {
              unbindAll() // lepas ikatan cameraX darimana saja
              bindToLifecycle(this@CameraActivity, cameraSelector, preview, imageCapture)
            }
          } catch (exc: Exception) {
            Toast.makeText(this@CameraActivity, "Gagal memunculkan kamera.", Toast.LENGTH_SHORT)
                .show()
          }
        },
        // diproses dalam main thread
        ContextCompat.getMainExecutor(this),
    )
  }

  private fun hideSystemUI() {
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
      window.setFlags(
          WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
    supportActionBar?.hide()
  }

  companion object {
    const val EXTRA_PICTURE = "picture"
    const val EXTRA_IS_BACK_CAMERA = "isBackCamera"
  }
}
