package com.Project.Project

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: EventDatabaseHelper
    private var eventLat: Double = 0.0
    private var eventLng: Double = 0.0
    private var eventName: String? = null
    private var showAllEvents = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dbHelper = EventDatabaseHelper(this)

        // ✅ Check if showing all events
        showAllEvents = intent.getBooleanExtra("showAllEvents", false)

        // ✅ Get single event data if available
        eventLat = intent.getDoubleExtra("latitude", 0.0)
        eventLng = intent.getDoubleExtra("longitude", 0.0)
        eventName = intent.getStringExtra("eventName")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showUserLocation() {
        map.isMyLocationEnabled = true

        if (showAllEvents) {
            // 🟡 Show all events from database
            dbHelper.fetchEvents(
                onResult = { events ->
                    for (event in events) {
                        val position = LatLng(event.latitude, event.longitude)
                        map.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(event.name)
                                .snippet(event.location)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        )
                    }
                    val defaultCenter = LatLng(43.945, -78.895)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 12f))
                },
                onError = {
                    val defaultCenter = LatLng(43.945, -78.895)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 12f))
                }
            )

        } else if (eventLat != 0.0 && eventLng != 0.0) {
            // 🟢 Show single event
            val eventLoc = LatLng(eventLat, eventLng)
            map.addMarker(
                MarkerOptions()
                    .position(eventLoc)
                    .title(eventName ?: "Event Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLoc, 15f))
        } else {
            // 🟣 Default fallback
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                val fallback = loc?.let { LatLng(it.latitude, it.longitude) }
                    ?: LatLng(43.945, -78.895)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(fallback, 12f))
            }
        }
    }
}
