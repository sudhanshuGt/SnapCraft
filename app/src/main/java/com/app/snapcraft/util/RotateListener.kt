package com.app.snapcraft.util

import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class RotateListener(private val mainView: View) : View.OnTouchListener {

    private var prevX: Float = 0f
    private var prevY: Float = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_DOWN")
                prevX = event.x
                prevY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_MOVE")

                // Calculate the differences between current and previous coordinates
                val deltaX = event.x - prevX
                val deltaY = event.y - prevY

                // Rotation based on move events
                val angle = atan2(deltaY.toDouble(), deltaX.toDouble()).toFloat()
                val rotation = mainView.rotation - Math.toDegrees(angle.toDouble()).toFloat()

                mainView.rotation = rotation
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_UP")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_POINTER_DOWN")
            }

            MotionEvent.ACTION_HOVER_EXIT -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_HOVER_EXIT")
            }

            MotionEvent.ACTION_CANCEL -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_CANCEL")
            }

            MotionEvent.ACTION_OUTSIDE -> {
                Log.i("--ROTATE_LISTENER--", "MotionEvent -> ACTION_OUTSIDE")
            }
        }

        return true
    }
}
