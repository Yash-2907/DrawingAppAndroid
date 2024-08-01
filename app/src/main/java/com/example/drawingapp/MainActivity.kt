package com.example.drawingapp

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        //DrawObj.setBrushSize(25F)
        findViewById<ImageButton>(R.id.brushbtn).setOnClickListener {
            val brushselector:Dialog=Dialog(this)
            brushselector.setContentView(R.layout.dialog_brushsizebox)
            brushselector.setTitle("Brush Size")
            brushselector.findViewById<ImageButton>(R.id.smallbtn).setOnClickListener{
                DrawObj.setBrushSize(10f)
                brushselector.dismiss()
            }
            brushselector.findViewById<ImageButton>(R.id.mediumbtn).setOnClickListener{
                DrawObj.setBrushSize(30f)
                brushselector.dismiss()
            }
            brushselector.findViewById<ImageButton>(R.id.largebtn).setOnClickListener{
                DrawObj.setBrushSize(50f)
                brushselector.dismiss()
            }
            brushselector.show()
        }
    }
}