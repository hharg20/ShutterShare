package ca.unb.mobiledev.shuttershare

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ca.unb.mobiledev.shuttershare.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraController: LifecycleCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)
        setContentView(viewBinding.root)

        //Possibly add this line here
//        replaceFragment(Camera())

        // Checking if permissions were granted in a previous session
//        if(!hasPermissions(baseContext)) {
//            // request camera-related permissions
//            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
//        } else {
//            startCamera()
//        }

        //viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        //Navigation
        replaceFragment(Camera())
        //val myBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        viewBinding.bottomNavigationView.setOnItemSelectedListener {
            Log.d("NAVBAR", "Nav item was selected")
            when(it.itemId){
                R.id.camera -> replaceFragment(Camera())
                R.id.photoAlbum -> replaceFragment(PhotoAlbums())

                else ->{

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}