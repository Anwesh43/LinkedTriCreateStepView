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
import android.hardware.camera2.params.ColorSpaceTransform

val nodes : Int = 3

val speed : Float = 0.025f

fun Canvas.drawTCSNode(i : Int, scale : Float, cb : (Canvas)-> Unit, paint : Paint) {
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
    translate( gap * sc2, 0f)
    cb(this)
    save()
    translate(i * gap + gap/2, h/2)
    rotate(deg * i)
    drawLine(-size/2 * sc1, y, size/2 * sc1, y, paint)
    restore()
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
            scale += dir * speed
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

    data class TCSNode(var i : Int, val state : State = State()) {

        private var next : TCSNode? = null

        private var prev : TCSNode? = null
        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TCSNode(i + 1)
                next?.prev = this
            }
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TCSNode {
            var curr : TCSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTCSNode(i, state.scale, {
                prev?.draw(it, paint)
            },paint)

        }
    }

    data class TriCreateStep(var i : Int) {

        private var dir : Int = 1

        private var curr : TCSNode = TCSNode(0)

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {i, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(i, scale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriCreateStepView) {

        private val animator : Animator = Animator(view)

        private val triCreateStep : TriCreateStep = TriCreateStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            triCreateStep.draw(canvas, paint)
            animator.animate {
                triCreateStep.update {i, scale ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            triCreateStep.startUpdating {
                animator.start()
            }
        }
    }
}