package com.anwesh.uiprojects.tricreatestepview

/**
 * Created by anweshmishra on 31/07/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

val nodes : Int = 3

fun Canvas.drawTCSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / nodes
    val deg : Float = 360f / nodes
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val size : Float = gap/3
    val y : Float = (size / 2) / Math.tan(deg.toDouble() / 2).toFloat()
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#3F51B5")
    save()
    translate(i * gap + gap/2 + gap * sc2, h / 2)
    rotate(deg * i)
    drawLine(-size/2 * sc1, y, size/2 * sc1, y, paint)
    restore()
}

class TriCreateStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * 0.1f
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}