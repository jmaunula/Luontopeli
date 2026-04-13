package com.example.luontopeli.viewmodel

import android.app.Application
import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = NatureSpotRepository(db.natureSpotDao())

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
        val outputFile = File(outputDir, "IMG_${timestamp}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _capturedImagePath.value = outputFile.absolutePath
                    _isLoading.value = false
                }

                override fun onError(exception: ImageCaptureException) {
                    _isLoading.value = false
                }
            }
        )
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        viewModelScope.launch {
            val spot = NatureSpot(
                name = "Luontoloyto",
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath
            )
            repository.insertSpot(spot)
            clearCapturedImage()
        }
    }
}