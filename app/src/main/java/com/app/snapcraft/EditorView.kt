package com.app.snapcraft

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.snapcraft.databinding.EditFrameViewBinding
import com.app.snapcraft.databinding.EditorViewBinding
import com.app.snapcraft.util.ImageTouchListener
import com.app.snapcraft.util.RotateListener
import com.app.snapcraft.util.ZoomInAndOutListener
import java.io.File
import java.io.FileOutputStream

class EditorView : AppCompatActivity() {

    private lateinit var binding: EditorViewBinding
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    addImageToLayout(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = EditorViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener {
            openGallery()
        }


        binding.btnSave.setOnClickListener {
            saveCompositeImage()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addImageToLayout(uri: Uri) {

        val bindingFrame : EditFrameViewBinding = EditFrameViewBinding.inflate(layoutInflater)
        val image = bindingFrame.frameImgView
        image.setImageURI(uri)

        bindingFrame.frameView.setOnClickListener {
            handleImageDrag(bindingFrame.frameView)
        }

        bindingFrame.frameRmBtn.setOnClickListener {
            handleRotationBtn(bindingFrame.frameRmBtn, bindingFrame.frameView)
        }

        bindingFrame.frameScBtn.setOnClickListener {
            Log.i("--EDITOR_VIEW--", "frameBtn clicked")
            handleScaleBtn(bindingFrame.frameScBtn, bindingFrame.frameView)
        }

        binding.layoutContainer.addView(bindingFrame.root, 500, 500)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleRotationBtn(frameRmBtn: ImageView, frameView: ConstraintLayout) {
        frameRmBtn.setOnTouchListener(RotateListener(frameView))
    }

    private fun handleScaleBtn(view: View, mainView : View) {
          view.setOnTouchListener(ZoomInAndOutListener(mainView))
     }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleImageDrag(view : View) {
        removeAllTouchListeners(binding.layoutContainer)

        val imageTouchListener = ImageTouchListener()
        view.setOnTouchListener(imageTouchListener)
    }

    private fun saveCompositeImage() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val compositeBitmap = Bitmap.createBitmap(
                binding.layoutContainer.width,
                binding.layoutContainer.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(compositeBitmap)
            canvas.drawColor(Color.WHITE)

            var hasImages = false

            for (i in 0 until binding.layoutContainer.childCount) {
                val child = binding.layoutContainer.getChildAt(i)
                if (child is ImageView) {
                    hasImages = true
                    val matrix = Matrix()
                    child.imageMatrix.invert(matrix)
                    canvas.save()
                    canvas.concat(matrix)
                    child.draw(canvas)
                    canvas.restore()
                }
            }

            if (hasImages) {
                saveBitmapToFile(compositeBitmap)
            } else {
                showToast("No images to save.")
            }
        } else {
            // Request storage permission
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "composite_image.jpg"
            )
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            showToast("Composite image saved: ${file.absolutePath}")
        } catch (e: Exception) {
            showToast("Error saving composite image.")
            e.printStackTrace()
        }
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


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
