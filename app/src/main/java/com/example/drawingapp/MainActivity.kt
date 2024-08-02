package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    var brushOReraser :Boolean=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        var lastvalbrush:Float=10f
        var lastvaleraser:Float=10f
        var defaultcolor=ContextCompat.getColor(this,R.color.black)
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
            brushOReraser=true
            findViewById<Slider>(R.id.sizeslider).value=lastvalbrush
            DrawObj.setBrushColor(defaultcolor)
        }
        findViewById<ImageButton>(R.id.eraserbtn).setOnClickListener{
            setselected(3)
            brushOReraser=false
            findViewById<Slider>(R.id.sizeslider).value=lastvaleraser
            val whitecolor=ContextCompat.getColor(this,R.color.white)
            DrawObj.setBrushColor(whitecolor)
        }
        findViewById<Slider>(R.id.sizeslider).addOnChangeListener{slider,value,fromUser->
            DrawObj.setBrushSize(value)
            if(brushOReraser) {
                lastvalbrush = value
            }
            else {
                lastvaleraser = value
            }
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
            1->{findViewById<ImageButton>(R.id.brushbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)
            }
            2->{findViewById<ImageButton>(R.id.colorpalletebtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)}
            3->{findViewById<ImageButton>(R.id.eraserbtn).background=ContextCompat.getDrawable(this,R.drawable.roundbtnselected)}
        }
    }
}