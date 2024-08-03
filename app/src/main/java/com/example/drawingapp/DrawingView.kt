package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context:Context,attr:AttributeSet): View(context,attr) {
    private var mdrawpath : CustomPath?=null
    private var mCanvasBitmap : Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mCanvasPaint : Paint?=null
    private var mBrushSize :Float = 0F
    private var color= Color.BLACK
    private var canvas: Canvas?=null
    private val allPath=ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }
    private fun setUpDrawing()
    {
        mdrawpath=CustomPath(color,mBrushSize)
        mDrawPaint=Paint()
        mDrawPaint!!.color = color
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint=Paint((Paint.DITHER_FLAG))
        mBrushSize=20F
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for(path in allPath)
        {
            mDrawPaint!!.strokeWidth= path.BrushThickness
            mDrawPaint!!.color=path.color
            canvas.drawPath(path, mDrawPaint!!)
        }
        if(!mdrawpath!!.isEmpty) {
            mDrawPaint!!.strokeWidth= mdrawpath!!.BrushThickness
            mDrawPaint!!.color=mdrawpath!!.color
            canvas.drawPath(mdrawpath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx=event?.x
        val touchy=event?.y
        when(event?.action)
        {
            MotionEvent.ACTION_DOWN->{
                mdrawpath!!.color=color
                mdrawpath!!.BrushThickness=mBrushSize
                mdrawpath!!.reset()
                mdrawpath!!.moveTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_MOVE->{
                mdrawpath!!.lineTo(touchx!!,touchy!!)
            }
            MotionEvent.ACTION_UP->{
                allPath.add(mdrawpath!!)
                mdrawpath=CustomPath(color,mBrushSize)
            }
            else->return false
        }
        invalidate()
        return true
    }
    public fun setBrushSize(newSize:Float)
    {
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }

    public fun setBrushColor(newColor: Int)
    {
       mDrawPaint!!.color=newColor
        color=newColor
        Log.d("ifcolorchanges","Color picker $newColor")
    }

    public fun undo()
    {
        if(!allPath.isEmpty())
        {
            allPath.removeAt(allPath.lastIndex)
            invalidate()
        }
    }
    private inner class CustomPath(var color:Int,var BrushThickness:Float): Path() {

    }
}