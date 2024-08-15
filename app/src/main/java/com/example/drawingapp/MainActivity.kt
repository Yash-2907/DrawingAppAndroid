package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog
import java.util.ArrayList
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    var brushOReraser :Boolean=true
    private val contract=registerForActivityResult(ActivityResultContracts.GetContent())
    {
        findViewById<ImageView>(R.id.tracelayer).setImageURI(it)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        var lastvalbrush:Float=10f
        var lastvaleraser:Float=10f
        var defaultcolor=ContextCompat.getColor(this,R.color.black)
        findViewById<ImageButton>(R.id.downloadbtn).setOnClickListener{
            val builder=AlertDialog.Builder(this)
            builder.setTitle("Save To Gallery")
            builder.setMessage("Do you want to save your drawing to your gallery ?!")
            builder.setIcon(R.drawable.savebtn)
            builder.setPositiveButton("Yes"){dialogueInterface,which->

                //HERE SAVE TO GALLERY WILL BE IMPLEMENTED


                dialogueInterface.dismiss()
            }
            builder.setNeutralButton("Cancel"){dialogueInterface,which->
                dialogueInterface.dismiss()
            }
            val alertDialog:AlertDialog=builder.create()
            alertDialog.show()
        }
        findViewById<ImageButton>(R.id.deleteallbtn).setOnClickListener{
            val builder=AlertDialog.Builder(this)
            builder.setTitle("Alert")
            builder.setMessage("Do you confirm to delete the entire canvas?")
            builder.setIcon(R.drawable.alertpng)
            builder.setPositiveButton("Yes"){dialogueInterface,which->
                DrawObj.clearcanvas()
                findViewById<ImageView>(R.id.tracelayer).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.whitebg))
                dialogueInterface.dismiss()
            }
            builder.setNeutralButton("Cancel"){dialogueInterface,which->
                dialogueInterface.dismiss()
            }
            val alertDialog:AlertDialog=builder.create()
            alertDialog.show()
        }
        findViewById<ImageButton>(R.id.colorpalletebtn).setOnClickListener{
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
            val roundedValue = String.format("%.1f", value)
            findViewById<TextView>(R.id.sizepercentage).text = "$roundedValue%"
            if(brushOReraser) {
                lastvalbrush = value
            }
            else {
                lastvaleraser = value
            }
        }
        findViewById<ImageButton>(R.id.undobtn).setOnClickListener{
            DrawObj.undo()
        }

        findViewById<ImageButton>(R.id.imagebtn).setOnClickListener {
            if(requestpermission())
            {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.mediapickerdialog)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.findViewById<ImageButton>(R.id.camerabtn).setOnClickListener{


                     //camera


                }
                dialog.findViewById<ImageButton>(R.id.gallerybtn).setOnClickListener{
                    contract.launch("image/*")
                    dialog.dismiss()
                }
                dialog.findViewById<ImageButton>(R.id.dustbinbtn).setOnClickListener{
                    findViewById<ImageView>(R.id.tracelayer).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.whitebg))
                    dialog.dismiss()
                }
                dialog.show()
            }
            else{
                Toast.makeText(this, "Oops Your Need To allow all those permissions to access this feature!!", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun requestpermission():Boolean{
        var listofpermissions =permissionlist()
        if(listofpermissions.isEmpty()) {
            return true
        }
        else{
            ActivityCompat.requestPermissions(this,listofpermissions.toTypedArray(),1)
            listofpermissions=permissionlist()
            if(permissionlist().isEmpty()){
                return true
            }
            else{
                return false
            }
        }
    }

    fun permissionlist():ArrayList<String>
    {
        var listofpermissions=ArrayList<String>()
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
        {
            listofpermissions.add(Manifest.permission.CAMERA)
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED)
        {
            listofpermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        return listofpermissions
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