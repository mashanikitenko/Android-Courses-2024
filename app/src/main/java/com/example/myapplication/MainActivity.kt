package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "Reached here")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhotoButton.setOnClickListener {
            takePhoto()
        }

        binding.sendMailButton.setOnClickListener {
            if (::photoFile.isInitialized) {
                sendEmailWithAttachment()
            } else {
                Toast.makeText(this, "Please take a selfie first", Toast.LENGTH_SHORT).show()
            }
        }

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), 100
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permissions granted!")
        } else {
            Toast.makeText(this, "Permissions are required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        Log.d("MainActivity", "Reached here")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            try {
                photoFile = createImageFile() ?: return
            } catch (e: Exception) {
                Log.e("MainActivity", "Error creating file for photo: ${e.message}")
                Toast.makeText(this, "Error creating photo file", Toast.LENGTH_SHORT).show()
                return
            }

            val photoURI = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            try {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting camera activity: ${e.message}")
                Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Log.e("MainActivity", "Camera not supported on this device")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val uri = Uri.fromFile(photoFile)
            binding.imageView.setImageURI(uri)  // Display the photo in the ImageView
        } else {
            Toast.makeText(this, "No photo taken", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            Log.d("MainActivity", "File created: ${imageFile.absolutePath}")
            imageFile
        } catch (e: Exception) {
            Log.e("MainActivity", "Error creating file: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun sendEmailWithAttachment() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Nikitenko Mariia]")
            putExtra(Intent.EXTRA_TEXT, "Repository link: https://github.com/mashanikitenko/Android-Courses-2024.git")
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", photoFile))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}


