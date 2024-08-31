package com.example.drawingapp

import android.Manifest
import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Objects
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var DrawObj:DrawingView
    var brushOReraser :Boolean=true
    private val contract=registerForActivityResult(ActivityResultContracts.GetContent())
    {
        findViewById<ImageView>(R.id.tracelayer).setImageURI(it)
    }
    private lateinit var imageUri : Uri
    private lateinit var shareuri : Uri
    private val camContract=registerForActivityResult(ActivityResultContracts.TakePicture())
    {
        findViewById<ImageView>(R.id.tracelayer).setImageURI(null)
        findViewById<ImageView>(R.id.tracelayer).setImageURI(imageUri)
    }
    @RequiresApi(Build.VERSION_CODES.P)
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
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val dialog = Dialog(this@MainActivity)
                        dialog.setContentView(R.layout.loadingscreen)
                        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        dialog.show()
                        withContext(Dispatchers.IO) {
                            saveInBg(getBitmapfromView(findViewById(R.id.Frameid)),"Drawing_File"+System.currentTimeMillis()/1000)
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
                                val finalDialog = Dialog(this@MainActivity).apply {
                                    setContentView(R.layout.sharedialog)
                                    window?.setBackgroundDrawableResource(android.R.color.transparent)
                                    findViewById<ImageButton>(R.id.sharebtn)?.setOnClickListener {
                                        shareImage(shareuri)
                                    }
                                    findViewById<ImageButton>(R.id.crossbtn).setOnClickListener{
                                        dismiss()
                                    }
                                    show()
                                }
                                finalDialog.setCanceledOnTouchOutside(false)
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
        findViewById<Slider>(R.id.sizeslider).addOnChangeListener { slider, value, fromUser ->
            DrawObj.setBrushSize(value)
            var circle=findViewById<ImageView>(R.id.brushsizecircle)
            val size :Int= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()
            val params: ViewGroup.LayoutParams = circle.layoutParams
            params.width = size
            params.height = size
            circle.layoutParams = params
            val roundedValue = String.format("%.1f", value)
            findViewById<TextView>(R.id.sizepercentage).text = "$roundedValue%"
            if (brushOReraser) {
                lastvalbrush = value
            } else {
                lastvaleraser = value
            }
        }
        findViewById<Slider>(R.id.sizeslider).addOnSliderTouchListener(object: Slider.OnSliderTouchListener{
            var circle=findViewById<ImageView>(R.id.brushsizecircle)
            override fun onStartTrackingTouch(slider: Slider) {
                val circlegd = circle.background as? GradientDrawable
                if (circlegd != null) {
                    circlegd.setColor(DrawObj.getcolor())
                }
                circle.visibility=View.VISIBLE
            }

            override fun onStopTrackingTouch(slider: Slider) {
                circle.visibility=View.INVISIBLE
            }
        })
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
                    imageUri=createImageUri()
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

        findViewById<ImageButton>(R.id.aibtn).setOnClickListener{
            val generativeModel =
                GenerativeModel(
                    // Specify a Gemini model appropriate for your use case
                    modelName = "gemini-1.5-flash",
                    // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                    apiKey = BuildConfig.api_key)

            val image: Bitmap = getBitmapfromView(findViewById(R.id.Frameid))
            val inputContent = content {
                image(image)
                text("This is a drawing , analyze it and solve/describe/suggest according to the input")
            }

            MainScope().launch {
                val response = generativeModel.generateContent(inputContent)
                Toast.makeText(this@MainActivity, "${response.text}", Toast.LENGTH_LONG).show()
            }
        }
    }



    suspend fun saveInBg(bitmap: Bitmap, fileName: String) {
        val fos: OutputStream?
        var imageUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Saving the image in MediaStore for Android Q and above
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Folder")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            // Saving the image in the external storage directory for Android versions below Q
            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Folder").toString()
            val imageFile = File(imageDir, "$fileName.jpg")
            imageUri = Uri.fromFile(imageFile)
            fos = FileOutputStream(imageFile)
        }
        shareuri=imageUri!!
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
        Objects.requireNonNull(fos)?.close()
    }

    private fun shareImage(imageUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/jpeg"  // Adjust the MIME type if needed
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION  // Grant permission to read the URI
        }
        startActivity(Intent.createChooser(shareIntent, "Share image via"))
    }
    private fun createImageUri():Uri{
        val image = File(filesDir,"drawingApp"+System.currentTimeMillis()/1000+".png")
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