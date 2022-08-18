package com.android.chat.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.chat.R
import com.android.chat.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : AppCompatActivity() {

    private var imageCapture : ImageCapture? = null
    private var videoCapture : VideoCapture? = null
    lateinit var binding : ActivityCameraBinding
    private var captureType = "image"
    private var CAMERA_SELECTOR = "back"
    private lateinit var outputDirectory: File
    private var pauseOffset = 0L

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 126)
            }
        } else {
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }

        binding.chronometerVideoRecord.format = "%s"
        binding.chronometerVideoRecord.base = SystemClock.elapsedRealtime()
        binding.btnCaptureVideo.setOnTouchListener { view, motionEvent ->

            when(motionEvent.action){

                MotionEvent.ACTION_DOWN ->{
                    binding.btnCaptureVideo.background = ContextCompat.getDrawable(this, R.drawable.camera_capture_design)

                    binding.chronometerVideoRecord.visibility = View.VISIBLE
                    binding.chronometerVideoRecord.base = SystemClock.elapsedRealtime() - pauseOffset
                    binding.chronometerVideoRecord.start()
                    captureVideo()
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP ->{
                    binding.btnCaptureVideo.background = null
                    videoCapture!!.stopRecording()

                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }

        binding.btnCaptureImage.setOnClickListener {
            binding.processImage.visibility = View.VISIBLE
            captureImage()
            binding.btnCaptureImage.isEnabled = false
        }

        outputDirectory = getOutputDirectory()

        binding.switchPicture.setOnClickListener {

            if (captureType == "image"){
                binding.switchPicture.setImageResource(R.drawable.ic_camera_white)
                captureType = "video"

                binding.btnCaptureImage.visibility = View.GONE
                binding.btnCaptureVideo.visibility = View.VISIBLE


            }else{
                binding.switchPicture.setImageResource(R.drawable.ic_video)
                captureType = "image"

                binding.btnCaptureImage.visibility = View.VISIBLE
                binding.btnCaptureVideo.visibility = View.GONE
            }
        }

        binding.flipCamera.setOnClickListener {
            CAMERA_SELECTOR = if (CAMERA_SELECTOR == "back"){
                startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                "front"

            }else{
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                "back"
            }
        }

        binding.cameraPreviewDelete.setOnClickListener {
            binding.constraintCamera.visibility = View.VISIBLE
            binding.constraintShowCapture.visibility = View.GONE
            binding.btnCaptureImage.isEnabled = true

        }

    }


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(output, ContextCompat.getMainExecutor(this), object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                binding.btnCaptureImage.isEnabled = true
                binding.processImage.visibility = View.GONE
                binding.constraintCamera.visibility = View.GONE
                binding.constraintShowCapture.visibility = View.VISIBLE
                binding.cameraPreviewImage.visibility = View.VISIBLE
                binding.cameraPreviewVideo.visibility = View.GONE
                binding.cameraPreviewDelete.visibility = View.VISIBLE
                binding.sendCapture.visibility = View.VISIBLE

                binding.cameraPreviewImage.setImageURI(outputFileResults.savedUri)

                binding.sendCapture.setOnClickListener {
                    val returnIntent = Intent()
                    returnIntent.putExtra("uri", outputFileResults.savedUri.toString())
                    returnIntent.putExtra("type", "image")
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }


                Log.e("onImageSaved", outputFileResults.savedUri.toString())


            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraActivity, exception.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    @SuppressLint("RestrictedApi")
    private fun captureVideo() {
        val videoCapture = videoCapture ?: return

        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".mp4")

        val output = VideoCapture.OutputFileOptions.Builder(photoFile).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 127)
            return
        }
        videoCapture.startRecording(output, ContextCompat.getMainExecutor(this), object :VideoCapture.OnVideoSavedCallback{
            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {

                binding.processImage.visibility = View.GONE
                binding.constraintCamera.visibility = View.GONE
                binding.constraintShowCapture.visibility = View.VISIBLE
                binding.cameraPreviewImage.visibility = View.GONE
                binding.cameraPreviewVideo.visibility = View.VISIBLE
                binding.cameraPreviewDelete.visibility = View.VISIBLE
                binding.sendCapture.visibility = View.VISIBLE

                binding.cameraPreviewVideo.setVideoURI(outputFileResults.savedUri)
                binding.cameraPreviewVideo.start()
                binding.cameraPreviewVideo.setMediaController(MediaController(this@CameraActivity))
                binding.chronometerVideoRecord.stop()

                binding.chronometerVideoRecord.stop()
                pauseOffset = SystemClock.elapsedRealtime() - binding.chronometerVideoRecord.base
                binding.chronometerVideoRecord.base = SystemClock.elapsedRealtime()
                pauseOffset = 0

                binding.sendCapture.setOnClickListener {
                    val returnIntent = Intent()
                    returnIntent.putExtra("uri", outputFileResults.savedUri.toString())
                    returnIntent.putExtra("type", "video")
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }

            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {

            }

        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 126 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }

        if (requestCode == 127 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            captureVideo()
        }
    }
    @SuppressLint("RestrictedApi")
    private fun startCamera(defaultBackCamera: CameraSelector) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 13)
            }
        }


        imageCapture = ImageCapture.Builder().build()

        videoCapture = VideoCapture.Builder().build()

        val cameraProviderFuture  = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,defaultBackCamera, preview, imageCapture,videoCapture)

            }catch (e:Exception){

            }

        }, ContextCompat.getMainExecutor(this))
    }

}