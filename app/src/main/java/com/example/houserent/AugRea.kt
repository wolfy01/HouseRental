package com.example.houserent

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import java.util.*

class AugRea : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var dbref: DatabaseReference

    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null

    private var overlayPaint: Paint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
    }

    private var addressOverlay: String = ""
    private var countOverlay: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aug_rea)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        surfaceHolder = surfaceView.holder
        surfaceHolder?.addCallback(this)
        findViewById<Button>(R.id.btn).setOnClickListener {
            checkLocation()
        }

    }

    private fun checkLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                102
            )
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val message = "Latitude: $latitude\nLongitude: $longitude"
                    val address = getAddressFromLocation(latitude, longitude)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    val message1 = "Address: $address"
                    Toast.makeText(this, message1, Toast.LENGTH_SHORT).show()
                    addressOverlay = address
                    dbref = FirebaseDatabase.getInstance().getReference("posts")
                    dbref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var count = 0
                                val addressParts = address?.split(" ")
                                for (postSnapshot in snapshot.children) {
                                    val post = postSnapshot.getValue(Post::class.java)
                                    if (post != null && addressParts != null) {
                                        for (part in addressParts) {
                                            if (part.isNotBlank() && post.space?.contains(part, ignoreCase = true) == true
                                                && !post.space.contains("sale", ignoreCase = true)
                                                && !post.space.contains("land", ignoreCase = true)
                                                && !post.space.contains("office", ignoreCase = true)
                                                && !post.space.contains("shop", ignoreCase = true)
                                            ) {
                                                count++
                                                break
                                            }
                                        }
                                    }
                                }
                                runOnUiThread {
                                    Toast.makeText(
                                        applicationContext,
                                        "Number of matching posts: $count",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    countOverlay = "$count rental houses available"
                                    updateOverlayText() // Update overlay text
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        var addressString = ""
        try {
            val addresses: List<Address> =
                geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val sb = StringBuilder()
                val road = address.thoroughfare
                val area = address.subLocality
                val city = address.subAdminArea
                if (!road.isNullOrBlank()) {
                    sb.append("Road: $road, ")
                }
                if (!area.isNullOrBlank()) {
                    sb.append("Area: $area, ")
                }
                if (!city.isNullOrBlank()) {
                    sb.append("City: $city")
                }
                addressString = area.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return addressString
    }

    private fun openCamera() {
        try {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            updateOverlayText() // Update overlay text
        }
    }

    private fun updateOverlayText() {
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        val surfaceViewWidth = surfaceView.width
        val surfaceViewHeight = surfaceView.height

        overlayPaint.textSize = surfaceViewWidth / 20f

        val textBounds = Rect()
        overlayPaint.getTextBounds(addressOverlay, 0, addressOverlay.length, textBounds)

        val textView = findViewById<TextView>(R.id.textView)
        textView.x = (surfaceViewWidth - textBounds.width()) / 2f
        textView.y = surfaceViewHeight * 0.4f
        textView.text = addressOverlay

        val countTextView = findViewById<TextView>(R.id.textView1)
        countTextView.x = (surfaceViewWidth - textBounds.width()) / 2f
        countTextView.y = surfaceViewHeight * 0.5f
        countTextView.text = countOverlay
    }

}
