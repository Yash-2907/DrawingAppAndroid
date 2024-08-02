package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        var lastval:Float=0f
        //DrawObj.setBrushSize(25F)
        var defaultcolor=ContextCompat.getColor(this,R.color.white)
        findViewById<ImageButton>(R.id.colorpalletebtn).setOnClickListener{
            setselected(2)
            val colorpicker:AmbilWarnaDialog=AmbilWarnaDialog(this,defaultcolor,object : AmbilWarnaDialog.OnAmbilWarnaListener{
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    defaultcolor=color
                    Log.d("ifcolorchanges","default color changes to :- $defaultcolor")
                    DrawObj.setBrushColor(color)
                }
            })
            colorpicker.show()
        }
        findViewById<ImageButton>(R.id.brushbtn).setOnClickListener {
            setselected(1)
            val brushselector:Dialog=Dialog(this)
            brushselector.setContentView(R.layout.dialog_brushsizebox)
            brushselector.setTitle("Brush Size")
            brushselector.findViewById<Slider>(R.id.sliderbtn).value=lastval
            brushselector.findViewById<Slider>(R.id.sliderbtn).addOnChangeListener{Slider,value,fromUser->
                DrawObj.setBrushSize(value)
                lastval=value
            }
            brushselector.show()
            brushselector.findViewById<ImageButton>(R.id.smallbtn).setOnClickListener{
                DrawObj.setBrushSize(20F)
                lastval=20f
                brushselector.dismiss()
            }
            brushselector.findViewById<ImageButton>(R.id.mediumbtn).setOnClickListener{
                DrawObj.setBrushSize(45F)
                lastval=45f
                brushselector.dismiss()
            }
            brushselector.findViewById<ImageButton>(R.id.largebtn).setOnClickListener{
                DrawObj.setBrushSize(70F)
                lastval=70f
                brushselector.dismiss()
            }
        }
        findViewById<ImageButton>(R.id.eraserbtn).setOnClickListener{
            setselected(3)
            val whitecolor=ContextCompat.getColor(this,R.color.white)
            DrawObj.setBrushColor(whitecolor)
        }
    }
    fun resetbtnselection()
    {
        findViewById<ImageButton>(R.id.brushbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtn)
        findViewById<ImageButton>(R.id.colorpalletebtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtn)
        findViewById<ImageButton>(R.id.eraserbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtn)
    }
    fun setselected(n:Int)
    {
        resetbtnselection()
        when(n){
            1->{findViewById<ImageButton>(R.id.brushbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)}
            2->{findViewById<ImageButton>(R.id.colorpalletebtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)}
            3->{findViewById<ImageButton>(R.id.eraserbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)}
        }
    }
}