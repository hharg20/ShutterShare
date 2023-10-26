package ca.unb.mobiledev.shuttershare

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import ca.unb.mobiledev.shuttershare.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var cameraController: LifecycleCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance().getReference("Test")
        storage = FirebaseStorage.getInstance().getReference("TestEvent")

        //setContentView(R.layout.activity_main)
        setContentView(viewBinding.root)

        // Checking if permissions were granted in a previous session
        if(!hasPermissions(baseContext)) {
            // request camera-related permissions
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startCamera()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        viewBinding.loginScreenButton.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }

        //Firebase Test code
        Toast.makeText(this, "Firebase Connection Successful", Toast.LENGTH_SHORT).show()

        val firstName = "John"
        val lastName = "Smith"
        val age = "33"
        val userName = "jsmith"


        val test = Test(firstName, lastName, age, userName)
        database.child(userName).setValue(test).addOnSuccessListener {
            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to Save", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startCamera() {
        val previewView: PreviewView = viewBinding.viewFinder
        cameraController = LifecycleCameraController(baseContext)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // sets the selfie camera the default
        previewView.controller = cameraController
    }

    private fun takePhoto() {
        // Create time stamped name and MediaStore entry
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.CANADA)
//            .format(System.currentTimeMillis())

        // Setting up the content values for MediaStore to save the photos to the device
        // MAY WANT TO CHANGE THIS LATER ON TO THE CLOUD STUFF...?
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ShutterShare-Image")
//            }
//        }

        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        // right now when a picture is taken, it uploads to firebase cloud
        cameraController.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun  onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    // setting the folder location in Firebase
                    val imageRef = storage.child("test.jpg")

                    // grabbing the image from memory and converting it to Byte data
                    var bitmap: Bitmap = image.convertImageProxyToBitmap()
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    var toastText = ""

                    // upload the image (Byte data) to Firebase cloud
                    var uploadTask = imageRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                        toastText = "Failure to Upload"
                    }.addOnSuccessListener { taskSnapshot ->
                        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                        toastText = "Successful Upload"
                    }.addOnCanceledListener {
                        toastText = "Canceled Upload"
                    }.addOnCompleteListener {
                        toastText = "Upload Complete"
                    }

                    Toast.makeText(this@MainActivity, toastText, Toast.LENGTH_SHORT).show()

                    image.close()
                }
            }
        )
    }

    fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true

            // check if all required permissions were granted
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if(!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                startCamera()
            }
        }

    companion object {
        private const val TAG = "ShutterShare"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS" // may change in the future...
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}