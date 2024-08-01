package com.example.drawingapp

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        var lastval:Float=0f
        //DrawObj.setBrushSize(25F)
        findViewById<ImageButton>(R.id.brushbtn).setOnClickListener {
            val brushselector:Dialog=Dialog(this)
            brushselector.setContentView(R.layout.dialog_brushsizebox)
            brushselector.setTitle("Brush Size")
            brushselector.findViewById<Slider>(R.id.sliderbtn).value=lastval
            brushselector.findViewById<Slider>(R.id.sliderbtn).addOnChangeListener{Slider,value,fromUser->
                DrawObj.setBrushSize(value)
                lastval=value
            }
            brushselector.show()
        }
    }
}