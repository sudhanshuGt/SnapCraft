package com.app.snapcraft.util

import android.content.res.Resources
import android.graphics.Matrix
import android.os.Handler
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible

class ImageTouchListener : View.OnTouchListener {

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private val MIN_ZOOM = 0.1f
    private val MAX_ZOOM = 3.0f

    private var mode = NONE
    private var startDistance = 0f

    private var lastZoomTime = 0L
    private val debounceInterval = 100 // Adjust this interval as needed


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mode = DRAG
                lastTouchX = event.rawX - view.translationX
                lastTouchY = event.rawY - view.translationY
                savedMatrix.set(matrix)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
                startDistance = calculateDistance(event)
                savedMatrix.set(matrix)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val newX = event.rawX - lastTouchX
                    val newY = event.rawY - lastTouchY

                    // Smoothly animate the translation
                    animateTranslation(view, newX, newY)
                } else

                    if (mode == ZOOM) {
                    val currentTime = SystemClock.uptimeMillis()
                    if (currentTime - lastZoomTime > debounceInterval) {
                        lastZoomTime = currentTime

                        val newDist = calculateDistance(event)
                        if (newDist > 100f) {
                            val scale = newDist / startDistance
                            // Limit scaling within MIN_ZOOM and MAX_ZOOM
                            val newScale = scale.coerceIn(MIN_ZOOM, MAX_ZOOM)

                            matrix.set(savedMatrix)
                            matrix.postScale(newScale, newScale, view.width / 2f, view.height / 2f)

                            // Get the scale from the matrix values
                            val values = FloatArray(9)
                            matrix.getValues(values)
                            val currentScaleX = values[Matrix.MSCALE_X]
                            val currentScaleY = values[Matrix.MSCALE_Y]

                            // Set transformation properties for any type of view
                            view.translationX = values[Matrix.MTRANS_X]
                            view.translationY = values[Matrix.MTRANS_Y]
                            view.scaleX = currentScaleX
                            view.scaleY = currentScaleY

                            // Increase or decrease the size of the view
                            val scaleMultiplier = newScale / currentScaleX
                            view.animate()
                                .scaleX(currentScaleX * scaleMultiplier)
                                .scaleY(currentScaleY * scaleMultiplier)
                                .setDuration(0)
                                .start()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
        }

        return true
    }

    private fun animateTranslation(view: View, newX: Float, newY: Float) {
        view.animate()
            .translationX(newX)
            .translationY(newY)
            .setDuration(0) // Duration 0 for smooth movement without animation
            .start()
    }

    private fun calculateDistance(event: MotionEvent): Float {
        if (event.pointerCount >= 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return 0f
    }

    private fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}
