package com.example.drawingapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        //DrawObj.setBrushSize(25F)
    }
}