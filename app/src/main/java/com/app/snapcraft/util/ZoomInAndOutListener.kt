// write a same class where listener will set to a view and will pass a view to class and that mainView will be rotate clock wise and anti clock wise based on move event
package com.app.snapcraft.util

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2


class ZoomInAndOutListener(private val mainView: View) :
    View.OnTouchListener {


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


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var scaleFactor = 1f
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("--ZOOM_LISTENER--", "MotionEvent -> ACTION_DOWN")
                // Store initial touch coordinates

            }


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
                    Log.i(
                        "--ZOOM_LISTENER--",
                        "Direction X: Moving left $deltaX, Direction Y: Moving up $deltaY"
                    )
                    if (!scaleFactor.isNaN() && scaleFactor != 0f) {
                        try {
                            mainView.scaleX /= scaleFactor
                            mainView.scaleY /= scaleFactor
                        } catch (e: Exception) {
                            e.message
                        }
                    }
                }

                if (deltaX > 0 && deltaY > 0) {
                    Log.i(
                        "--ZOOM_LISTENER--",
                        "Direction X: Moving right $deltaX, Direction Y: Moving down $deltaY"
                    )
                    if (!scaleFactor.isNaN() && scaleFactor != 0f) {
                        try {
                            mainView.scaleX *= scaleFactor
                            mainView.scaleY *= scaleFactor
                        } catch (e: Exception) {
                            e.message
                        }
                    }
                }

                // Rotation based on move events
                val crossProduct = deltaX * (event.y - 0) - (event.x - 0) * deltaY
                val rotation = mainView.rotation + if (crossProduct > 0) 5f else -5f

                // Apply rotation
                mainView.rotation = rotation
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
