package com.example.arplacer.activityMain

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.arplacer.MyUtils
import com.example.arplacer.R
import com.example.arplacer.entity.AdditionalInformResponse
import com.example.arplacer.entity.LocationInformationResponse
import com.example.arplacer.network.LocationInformationRepository
import com.google.android.gms.location.*
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private var installRequested = false
    private var hasFinishedLoading = false
    private lateinit var arSceneView: ArSceneView
    private lateinit var placeInformationRenderable: ViewRenderable
    private var locationScene: LocationScene? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val placeLiveData = MutableLiveData<MutableList<LocationInformationResponse>>()
    private val addInformLiveData = MutableLiveData<AdditionalInformResponse>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val PERMISSION_ID = 1010
    private var longitude: Double? = null
    private var latitude: Double? = null

    private var onTap = false

    private lateinit var placeData:MutableList<LocationInformationRepository>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arSceneView = findViewById(R.id.ar_scene_view)

        viewModel =
            ViewModelProvider(this, MainViewModelFactory(this, LocationInformationRepository))
                .get(MainViewModel::class.java)
        viewModel.placeLiveData.observe(this, observer)
        coroutineScope.launch {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this@MainActivity)
            RequestPermission()
            getLastLocation()
            if (longitude != null && latitude != null) {
                val result =
                    LocationInformationRepository.getLocationInformationService().getLocationInform(
                        3000,
                        27.4343001,
                        53.8542905,
                        getString(R.string.response_type),
                        getString(R.string.opentripmap_key)
                    )
                placeLiveData.postValue(result)
            }
        }

        val placeLayout =
            ViewRenderable.builder()
                .setView(this, R.layout.object_data_layout)
                .build()

        CompletableFuture.allOf(placeLayout)
            .handle { t, u ->
                try {
                    placeInformationRenderable = placeLayout.get()
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                } catch (ex: ExecutionException) {
                }
            }

        arSceneView.scene
            .addOnUpdateListener {

                if (!hasFinishedLoading) {
                    return@addOnUpdateListener
                }
                if (locationScene == null) {

                    locationScene = LocationScene(this, arSceneView)

//                    locationScene?.setOffsetOverlapping(true);
//                    locationScene?.anchorRefreshInterval = 10000;

                    // questionLiveData.value?.forEach {
//                        val lon = it.point?.lon
//                        val lat = it.point?.lat
//                        val name = it.name
                    val lon = placeLiveData.value?.get(6)?.point?.lon
                    val lat = placeLiveData.value?.get(6)?.point?.lat
                    val name = placeLiveData.value?.get(6)?.name
                    val xid = placeLiveData.value?.get(6)?.xid

                    val layoutLocationMarker = LocationMarker(
                        lon!!,
                        lat!!,
                        placeInformationView(xid)
                    )
                    layoutLocationMarker.renderEvent =
                        LocationNodeRender { node ->
                            val eView: View = placeInformationRenderable.view
                            val name_place = eView.findViewById<TextView>(R.id.name_place)
                            val distanceTextView = eView.findViewById<TextView>(R.id.distance)
                            val text = eView.findViewById<TextView>(R.id.text_place)
                            val image_place = eView.findViewById<ImageView>(R.id.image_place)
                            if (onTap == false) {
                                distanceTextView.text = node.distance.toString() + "M"
                                name_place.text = name
                            } else {
                                Glide
                                    .with(this)
                                    .load(addInformLiveData.value?.preview)
                                    .into(image_place)
                                name_place.text = addInformLiveData.value?.name
                                text.text = addInformLiveData.value?.wikipedia_extracts.toString()
                                distanceTextView.text = node.distance.toString() + "M"
                            }


                        }

                    locationScene?.mLocationMarkers?.add(layoutLocationMarker)
                    // }
                }

                val frame: Frame? = arSceneView.arFrame

                if (locationScene != null) {
                    locationScene?.processFrame(frame)
                }
            }
        ARLocationPermissionHelper.requestPermission(this)
    }

    private val observer = Observer<MutableList<LocationInformationResponse>> { list ->

    }

    private fun placeInformationView(xid: String?): Node {
        val base = Node()
        base.renderable = placeInformationRenderable
        val view = placeInformationRenderable.view
        view.setOnClickListener {
            coroutineScope.launch {
                val res = LocationInformationRepository.getLocationInformationService()
                    .getMoreInformAboutPlace(xid, getString(R.string.opentripmap_key))
                addInformLiveData.postValue(res)
            }
            onTap = true
        }
        return base
    }

    private fun CheckPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun RequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }


    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (CheckPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        NewLocationData()
                    } else {
                        longitude = location.longitude
                        latitude = location.latitude
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            RequestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    fun NewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            longitude = lastLocation.longitude
            latitude = lastLocation.latitude
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationScene != null) {
            locationScene?.resume()
        }
        if (arSceneView.session == null) {
            try {
                val session = MyUtils().createArSession(this, installRequested)
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this)
                    return
                } else {
                    arSceneView.setupSession(session)
                }
            } catch (e: UnavailableException) {
            }
        }

        try {
            arSceneView.resume()
        } catch (ex: CameraNotAvailableException) {
            finish()
            return
        }
    }

    override fun onPause() {
        super.onPause()
        if (locationScene != null) {
            locationScene?.pause()
        }
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                ARLocationPermissionHelper.launchPermissionSettings(this)
            } else {
                Toast.makeText(
                    this, "Camera permission is needed to run this application", Toast.LENGTH_LONG
                )
                    .show()
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window
                .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}