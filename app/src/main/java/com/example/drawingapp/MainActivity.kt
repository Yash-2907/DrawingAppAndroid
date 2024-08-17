package com.example.drawingapp

import android.Manifest
import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.util.ArrayList
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    var brushOReraser :Boolean=true
    private val contract=registerForActivityResult(ActivityResultContracts.GetContent())
    {
        findViewById<ImageView>(R.id.tracelayer).setImageURI(it)
    }
    private lateinit var imageUri : Uri
    private val camContract=registerForActivityResult(ActivityResultContracts.TakePicture())
    {
        findViewById<ImageView>(R.id.tracelayer).setImageURI(imageUri)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DrawObj=findViewById<DrawingView>(R.id.Canvasid)
        var lastvalbrush:Float=10f
        var lastvaleraser:Float=10f
        var defaultcolor=ContextCompat.getColor(this,R.color.black)
        imageUri=createImageUri()
        findViewById<ImageButton>(R.id.downloadbtn).setOnClickListener{
            val builder=AlertDialog.Builder(this)
            builder.setTitle("Save To Gallery")
            builder.setMessage("Do you want to save your drawing to your gallery ?!")
            builder.setIcon(R.drawable.savebtn)
            builder.setPositiveButton("Yes"){dialogueInterface,which->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val dialog = Dialog(this@MainActivity)
                        dialog.setContentView(R.layout.loadingscreen)
                        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        dialog.show()
                        withContext(Dispatchers.IO) {
                            saveInBg()
                        }
                        dialog.dismiss()
                        val tickDialog = Dialog(this@MainActivity)
                        tickDialog.setContentView(R.layout.tickpopup)
                        tickDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        tickDialog.show()
                        val tickAnimationView = tickDialog.findViewById<LottieAnimationView>(R.id.tickAnime)
                        tickAnimationView.speed=1.5f
                        tickAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                Log.d("TickAnimation", "Animation started")
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                tickDialog.dismiss()
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                Log.d("TickAnimation", "Animation canceled")
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                Log.d("TickAnimation", "Animation repeated")
                            }
                        })
                    } catch (e: Exception) {
                        Log.e("DialogError", "Error during dialog or animation setup", e)
                    }
                }
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
                    camContract.launch(imageUri)
                    dialog.dismiss()
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
                Toast.makeText(this, "Oops Your Need To allow all permissions to access this feature!!", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun saveInBg(){
        delay(3000)
    }

    private fun createImageUri():Uri{
        val image = File(filesDir,"camera_photos.png")
        return FileProvider.getUriForFile(this,
            "com.example.drawingapp.FileProvider",
            image)
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

    fun getBitmapfromView(view: View):Bitmap{
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
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