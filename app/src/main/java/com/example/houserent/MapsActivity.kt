package com.example.houserent

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.houserent.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import kotlin.math.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseRef: DatabaseReference
    private val rentalHousePoints: MutableList<LatLng> = mutableListOf()

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val EARTH_RADIUS = 6371.0 // in kilometers
        private const val RADIUS_THRESHOLD = 500.0 // in meters
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
                retrieveDetailedPosts()
            }
        }
    }

    private fun retrieveDetailedPosts() {
        databaseRef = FirebaseDatabase.getInstance().getReference("Detailedposts")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val latitudeString = postSnapshot.child("latitude").getValue(String::class.java)
                    val longitudeString = postSnapshot.child("longitude").getValue(String::class.java)

                    if (latitudeString != null && longitudeString != null) {
                        val latitude = latitudeString.toDouble()
                        val longitude = longitudeString.toDouble()
                        val postLatLng = LatLng(latitude, longitude)
                        val distance = calculateDistance(lastLocation.latitude, lastLocation.longitude, latitude, longitude)

                        if (distance <= RADIUS_THRESHOLD) {
                            placeMarkerOnMap(postLatLng)
                            rentalHousePoints.add(postLatLng)
                        }
                    }
                }
                drawPolyline()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun calculateDistance(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS * c

        return distance * 1000 // Convert to meters
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap.addMarker(markerOptions)
    }

    private fun drawPolyline() {
        if (rentalHousePoints.size >= 1) {
            val polylineOptions = PolylineOptions()
            polylineOptions.color(Color.BLUE)
            polylineOptions.width(5f)
            polylineOptions.add(LatLng(lastLocation.latitude, lastLocation.longitude))
            polylineOptions.addAll(rentalHousePoints)

            mMap.addPolyline(polylineOptions)

            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(LatLng(lastLocation.latitude, lastLocation.longitude))
            for (point in rentalHousePoints) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            val padding = 100 // Adjust the padding as needed
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(cameraUpdate)
        }
    }

    override fun onMarkerClick(p0: Marker) = false
}