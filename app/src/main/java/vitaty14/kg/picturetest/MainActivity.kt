package vitaty14.kg.picturetest

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }

    private lateinit var path: String
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/KG")
    val file = (storageDir.getAbsolutePath()+"/KG_test.jpg")
    val imageFileName = "KG_test.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("debug",storageDir.absolutePath)
        Log.d("debug",file)
    }

    override fun onResume() {
        super.onResume()
            btnLaunchCamera.setOnClickListener {

                Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(packageManager)?.let {
                    if (checkPermission()) {
                        Toast.makeText(this, "start camera application", Toast.LENGTH_LONG).show()
                        takePicture()
                    } else {
                        grantCameraPermission()
                    }
                } ?: Toast.makeText(this, "camera application not found.", Toast.LENGTH_LONG).show()
            }

        var checkfile = File(file)
        var fileExists = checkfile.exists()

        if(fileExists) {
            path = storageDir.getAbsolutePath() + "/KG_test.jpg"

            try {
                val inputStream = FileInputStream(File(path))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                cameraImage.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.d("debug","FileNotFoundException")
            }
            Log.d("debug", "file_OK")
            textView.text = "file_OK"
        }else{
            Log.d("debug","file_empty")
            Toast.makeText(this, "file not found,please push button.", Toast.LENGTH_LONG).show()
            textView.text = "file_empty"
            }
        }

    private fun takePicture() {
        Log.d("debug","takePicture")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(MediaStore.EXTRA_OUTPUT, createSaveFileUri())
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun createSaveFileUri(): Uri {

        if (!storageDir.exists()) {
            storageDir.mkdir()
        }

        val file = File(storageDir,imageFileName)
        FileOutputStream(file)

        path = file.absolutePath
        return FileProvider.getUriForFile(this, "Picturetest.fileprovider", file)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("debug","onActivityResult")
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put("_data", path)
            }
            contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            val inputStream = FileInputStream(File(path))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            cameraImage.setImageBitmap(bitmap)
        }
    }

    private fun checkPermission(): Boolean {
        Log.d("debug","checkPermission")
        val cameraPermission = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

        val extraStoragePermission = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)

        return cameraPermission && extraStoragePermission
    }

    private fun grantCameraPermission() =
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            CAMERA_PERMISSION_REQUEST_CODE)

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        var isGranted = true
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false
                    }
                }
            } else {
                isGranted = false
            }
        } else {
            isGranted = false
        }

        if (isGranted) {
            takePicture()
        } else {
            grantCameraPermission()
        }
        Log.d("debug","onRequestPermissionsResult_end")
    }
}
