package com.app.snapcraft.util

import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.sqrt

class ZoomInAndOutListener(private val imageView: View, private val frame : ConstraintLayout) : View.OnTouchListener {


    private var prevY: Float = 0f
    private var prevX: Float = 0f
    private var startDistance = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    private val MIN_ZOOM = 0.1f
    private val MAX_ZOOM = 3.0f

    private var lastZoomTime = 0L
    private val debounceInterval = 100 // Adjust this interval as needed

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var scaleFactor = 1f
         when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_DOWN")
                // Store initial touch coordinates

            }
 // Inside your MotionEvent.ACTION_MOVE section
              MotionEvent.ACTION_MOVE -> {
                 Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_MOVE")

                 // Calculate the differences between current and previous coordinates
                 val deltaX = event.x - prevX
                 val deltaY = event.y - prevY

                 prevX = event.x
                 prevY = event.y

                 val diagonal = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()

                 val sensitivity = 1000f // Adjust this value based on your requirement
                 val scaleFactor = 1 + diagonal / sensitivity

                 // Apply scaling
                 if (deltaX < 0 && deltaY < 0) {
                     Log.i("--ZOOM_LISTENER--", "Direction X: Moving left $deltaX, Direction Y: Moving up $deltaY")
                     imageView.scaleX /= scaleFactor
                     imageView.scaleY /= scaleFactor
                     imageView.invalidateOutline()
                     imageView.invalidate()
                     imageView.requestLayout()
                  }

                 if (deltaX > 0 && deltaY > 0) {
                     Log.i("--ZOOM_LISTENER--", "Direction X: Moving right $deltaX, Direction Y: Moving down $deltaY")
                     imageView.scaleX *= scaleFactor
                     imageView.scaleY *= scaleFactor
                     imageView.invalidateOutline()
                     imageView.invalidate()
                     imageView.requestLayout()
                  }


             }


            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_UP")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_POINTER_DOWN")
            }

            MotionEvent.ACTION_HOVER_EXIT -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_HOVER_EXIT")
            }

            MotionEvent.ACTION_CANCEL -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_CANCEL")
            }

            MotionEvent.ACTION_OUTSIDE -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_OUTSIDE")
            }
        }

        return true
    }

    private fun calculateDistance(event: MotionEvent): Float {
        if (event.pointerCount >= 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return 0f
    }

}
