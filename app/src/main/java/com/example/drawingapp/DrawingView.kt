package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class DrawingView(context: Context, attr: AttributeSet) : View(context, attr) {

    private var mdrawpath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0F
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val allPath = ArrayList<CustomPath>()

    // For pinch-to-zoom
    private var scaleFactor = 1.0f
    private var scaleFocusX = 0f
    private var scaleFocusY = 0f
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var isDrawing = false

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mdrawpath = CustomPath(color, mBrushSize)
        mDrawPaint = Paint().apply {
            color = this@DrawingView.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20F
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Save the canvas state before applying transformations
        canvas.save()

        // Apply scaling transformation
        canvas.scale(scaleFactor, scaleFactor, scaleFocusX, scaleFocusY)

        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in allPath) {
            mDrawPaint!!.strokeWidth = path.BrushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mdrawpath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mdrawpath!!.BrushThickness
            mDrawPaint!!.color = mdrawpath!!.color
            canvas.drawPath(mdrawpath!!, mDrawPaint!!)
        }

        // Restore the canvas state
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        // Handle scaling
        scaleGestureDetector.onTouchEvent(event)

        if (!scaleGestureDetector.isInProgress) {
            val touchX = (event.x - scaleFocusX) / scaleFactor + scaleFocusX
            val touchY = (event.y - scaleFocusY) / scaleFactor + scaleFocusY

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isDrawing) {
                        isDrawing = true
                        mdrawpath!!.color = color
                        mdrawpath!!.BrushThickness = mBrushSize
                        mdrawpath!!.reset()
                        mdrawpath!!.moveTo(touchX, touchY)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDrawing) {
                        mdrawpath!!.lineTo(touchX, touchY)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isDrawing) {
                        allPath.add(mdrawpath!!)
                        mdrawpath = CustomPath(color, mBrushSize)
                        isDrawing = false
                    }
                }
                else -> return false
            }
        }
        invalidate()
        return true
    }

    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setBrushColor(newColor: Int) {
        mDrawPaint!!.color = newColor
        color = newColor
    }

    fun undo() {
        if (allPath.isNotEmpty()) {
            allPath.removeAt(allPath.lastIndex)
            invalidate()
        }
    }

    fun clearcanvas() {
        allPath.clear()
        invalidate()
    }

    private inner class CustomPath(var color: Int, var BrushThickness: Float) : Path()

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            // Capture the focal point of the scale gesture
            scaleFocusX = detector.focusX
            scaleFocusY = detector.focusY
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor

            // Apply scale
            scaleFactor *= scale
            scaleFactor = maxOf(0.1f, minOf(scaleFactor, 10.0f))

            // Update the focal point
            scaleFocusX = detector.focusX
            scaleFocusY = detector.focusY

            invalidate()
            return true
        }

        private val handler = Handler(Looper.getMainLooper())
        private val undoDelayMillis = 50L  // 50 milliseconds delay

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)

            // Use a Handler to post the undo task with a delay
            handler.postDelayed({
                undo()
            }, undoDelayMillis)
        }
    }
}
