package com.example.gpsmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gpsmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val fusedLocationProviderClient by lazy {
        FusedLocationProviderClient(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted -> if(isGranted){
            addLocationListener()
    }else{
        Toast.makeText(this,"권한이 거부되었습니다.",Toast.LENGTH_SHORT).show()
    }
    }

    private val locationRequest by lazy {
        com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

            interval = 10000

            fastestInterval = 5000
        }
    }
    private val locationCallback = MyLocationCallBack()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // supportMapFragment를 가져와 지도가 준비되면 알람.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onPause() {
        super.onPause()
        removeLocationListener()
    }

    private fun removeLocationListener(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        checkPermission(
            cancel = {
                showPermissionInfoDialog()
            },
            ok={
                addLocationListener()
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun addLocationListener(){
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback,null
        )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    inner class MyLocationCallBack : LocationCallback(){
        private  val polylineOptions = PolylineOptions().width(5f).color(Color.RED)

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            val location = locationResult?.lastLocation

            location?.run{
                val latLng = LatLng(latitude, longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17f))
                Log.d("MapsActivity","위도: $latitude 경도: $longitude")
                polylineOptions.add(latLng)
            }


            mMap.addPolyline(polylineOptions)
        }
    }



    private fun checkPermission(cancel:()->Unit, ok:()-> Unit){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )){
                cancel()
            }else{
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            return
        }
        ok()
    }
    private fun showPermissionInfoDialog(){
        AlertDialog.Builder(this).apply {
            setTitle("권한이 필요한 이유")
            setMessage("지도에 위치를 표시하려면 위치정보 권한이 필요합니다")
            setPositiveButton("권한요청") {_, _->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            setNegativeButton("거부",null)
        }.show()
    }
    // 콜백메서드 작성


}