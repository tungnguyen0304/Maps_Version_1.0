package com.example.mapsver10

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapsver10.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*


@Suppress("PrivatePropertyName")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val tag = MapsActivity::class.java.simpleName

    // creating variables for
    // edit texts and button.
    private lateinit var sourceEdt: EditText
    private lateinit var destinationEdt: EditText
    private lateinit var trackBtn: Button

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(tag, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(tag, "Can't find style. Error: ", e)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A snippet is additional text that's displayed after the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f" ,
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.showInfoWindow()
        }
    }

    private val RequestLocationPermission = 1
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                RequestLocationPermission
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initializing our edit text and buttons
        sourceEdt = findViewById(R.id.idEdtSource)
        destinationEdt = findViewById(R.id.idEdtDestination)
        trackBtn = findViewById(R.id.idBtnTrack)

        // adding on click listener to our button.
        // adding on click listener to our button.
        trackBtn.setOnClickListener { // calling a method to draw a track on google maps.
            drawTrack(sourceEdt.text.toString(), destinationEdt.text.toString())
        }
    }

    private fun drawTrack(source: String, destination: String) {
        try {
            // create a uri
            val uri = Uri.parse("https://www.google.co.in/maps/dir/$source/$destination")

            // initializing a intent with action view.
            val i = Intent(Intent.ACTION_VIEW, uri)

            // below line is to set maps package name
            i.setPackage("com.google.android.apps.maps")

            // below line is to set flags
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // start activity
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            // when the google maps is not installed on users device
            // we will redirect our user to google play to download google maps.
            val uri =
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps")

            // initializing intent with action view.
            val i = Intent(Intent.ACTION_VIEW, uri)

            // set flags
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // to start activity
            startActivity(i)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, "Map is Ready",  Toast.LENGTH_SHORT).show()
        Log.d(tag, "onMapReady: map is ready")
        map = googleMap

        val home = LatLng(10.868173, 106.637558)
        val zoomLevel = 15f
        map.addMarker(MarkerOptions().position(home).title("Home"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home,zoomLevel))

        val cameraPosition = CameraPosition.Builder()
            .target(home)
            .zoom(15f).bearing(67f).tilt(45f).build()

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        map.addCircle(
            CircleOptions()
                .center(home)
                .radius(500.0)
                .strokeWidth(3f)
                .strokeColor(Color.BLACK)
                .fillColor(Color.argb(70,150,50,50))
        )

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestLocationPermission) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

}