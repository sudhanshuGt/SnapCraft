package com.app.snapcraft

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.snapcraft.databinding.EditFrameViewBinding
import com.app.snapcraft.databinding.EditorViewBinding
import com.app.snapcraft.util.ImageTouchListener
import com.app.snapcraft.util.ZoomInAndOutListener
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EditorView : AppCompatActivity() {

    private lateinit var binding: EditorViewBinding
    private val TAG = "imageSaveFunction"
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    addImageToLayout(uri)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = EditorViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun listener() {
        binding.btnSelectImage.setOnClickListener {
            openGallery()
        }
        binding.btnSave.setOnClickListener {
            saveBitmapToGallery(
                this,
                binding.layoutContainer.toBitmap(),
                "snapcraft_" + getCurrentDateTime().ifEmpty { generateRandomString() })
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addImageToLayout(uri: Uri) {

        val bindingFrame: EditFrameViewBinding = EditFrameViewBinding.inflate(layoutInflater)
        val image = bindingFrame.frameImgView
        image.setImageURI(uri)

        bindingFrame.frameView.setOnClickListener {
            handleImageDrag(bindingFrame.frameView)
        }



        bindingFrame.frameScBtn.setOnTouchListener(ZoomInAndOutListener(bindingFrame.frameView))


        binding.layoutContainer.addView(bindingFrame.root, 500, 500)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun handleImageDrag(view: View) {
        removeAllTouchListeners(binding.layoutContainer)

        val imageTouchListener = ImageTouchListener()
        view.setOnTouchListener(imageTouchListener)
    }


    private fun removeAllTouchListeners(frameLayout: FrameLayout) {
        for (i in 0 until frameLayout.childCount) {
            val child = frameLayout.getChildAt(i)
            if (child is ImageView) {
                child.setOnTouchListener(null)
            }
        }
    }


    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100
    }


    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        try {
            // Check if the app has permission to write to external storage
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission if not granted
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION
                )
                Log.e(TAG, "Permission denied: WRITE_EXTERNAL_STORAGE")
                return
            }

            // Use MediaStore API for Android 10 and higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + "SnapCraft"
                    )
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                }

                val contentUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                contentUri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        // Save the bitmap to the output stream
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    Log.d(TAG, "Image saved successfully: $contentUri")
                } ?: run {
                    Log.e(TAG, "Error saving image to gallery: Failed to create content Uri")
                }
            } else {
                // For versions lower than Android 10, use traditional file approach
                val picturesDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDirectory = File(picturesDirectory, "SnapCraft")

                // Create the app directory if it doesn't exist
                if (!appDirectory.exists() && !appDirectory.mkdirs()) {
                    // Log an error and return if directory creation fails
                    Log.e(TAG, "Error creating directory: ${appDirectory.absolutePath}")
                    return
                }

                val imageFile = File(appDirectory, fileName)
                val outputStream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Add the image to the gallery for versions lower than Android 10
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
                }

                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                Log.d(TAG, "Image saved successfully: ${imageFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image to gallery: ${e.message}")
        }
    }

    private fun View.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMM_hhmmssa")
        return currentDateTime.format(formatter)
    }

    private fun generateRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }


}
