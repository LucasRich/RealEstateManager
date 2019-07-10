package com.openclassrooms.realestatemanager.controllers.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.location.*
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.view.PropertyViewModel
import kotlinx.android.synthetic.main.fragment_map_view.view.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.injection.Injection
import com.openclassrooms.realestatemanager.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class MapViewFragment : Fragment(), OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener{
    private lateinit var mMap: GoogleMap
    private var mLocationManager: LocationManager? = null
    internal var mMarker: Marker? = null

    private lateinit var propertyViewModel: PropertyViewModel
    private lateinit var menu: Menu

    private val PERMS = Manifest.permission.ACCESS_FINE_LOCATION
    private val LOCATION_PERMS = 100

    private var currentPosition: LatLng? = null


    companion object {
        fun newInstance(): MapViewFragment {
            return MapViewFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map_view, container, false)

        configureViewModel()
        rootView.mapView.onCreate(savedInstanceState)
        rootView.mapView.onResume() // needed to get the map to display immediately

        rootView.mapView.getMapAsync(this)

        rootView.myLocationButton.setOnClickListener {
            askPermissionsAndShowMyLocation()
            getCurrentLocationAndZoomOn()
            getAllPropertyAndDisplayWithMarker()
        }

        return rootView
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setOnMarkerClickListener(this)

        askPermissionsAndShowMyLocation()
        getCurrentLocationAndZoomOn()
        getAllPropertyAndDisplayWithMarker()
        setToolbarTitle(activity!!, "Map")
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------

    private fun configureViewModel() {
        val mViewModelFactory = Injection.provideViewModelFactory(context!!)
        this.propertyViewModel = ViewModelProviders.of(this, mViewModelFactory).get(PropertyViewModel::class.java)
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu)
        if (!Utils.isInternetAvailable()) menu.getItem(1).icon = ContextCompat.getDrawable(context!!, R.drawable.ic_wifi_off)
        super.onCreateOptionsMenu(menu, inflater)
    }*/

    // --------------------
    // MARKER
    // --------------------

    override fun onMarkerClick(mMarker: Marker): Boolean {
        if (mMarker == mMarker)
            launchDisplayPropertyFragment(mMarker.title.toLong())
        mMarker.hideInfoWindow()
        return true
    }
    // --------------------
    // CURRENT LOCATION
    // --------------------

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val criteria = Criteria()
        mLocationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = mLocationManager!!.getBestProvider(criteria, true)

        if (!EasyPermissions.hasPermissions(context!!, PERMS)) {
            EasyPermissions.requestPermissions(this, "Accès à la location", LOCATION_PERMS, PERMS)
            return
        }

        val location = mLocationManager!!.getLastKnownLocation(provider)

        if (location != null) {
            currentPosition = LatLng(location.latitude, location.longitude)
        }
    }

    private fun getCurrentLocationAndZoomOn() {
        try {
            getCurrentLocation()

            val cameraPosition = CameraPosition.Builder()
                    .target(currentPosition)
                    .zoom(17f)
                    .build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } catch (ignored: Exception) {}
    }

    override fun onLocationChanged(p0: Location?) {}

    // --------------------
    // PERMISSION
    // --------------------

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("MissingPermission")
    private fun askPermissionsAndShowMyLocation() {

        if (!EasyPermissions.hasPermissions(context!!, PERMS)) {
            EasyPermissions.requestPermissions(this, "Accès à la location", LOCATION_PERMS, PERMS)
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
    }

    // ---------------------
    // UTILS
    // ---------------------

    private fun getLatLngToAddress(location: String): Address? {
        var address: Address? = null

        var gc: Geocoder  = Geocoder(context)
        var addresses: List<Address> = gc.getFromLocationName(location, 1) // get the found Address Objects

        for (a: Address in addresses) {
            if (a.hasLatitude() && a.hasLongitude()) address = a
        }
        return address
    }

    private fun getAllPropertyAndDisplayWithMarker() {
        this.propertyViewModel.getAllProperty().observe(this, Observer<List<Property>> {propertyList  -> displayAllPropertyWithMarker(propertyList) })
    }

    private fun displayAllPropertyWithMarker(propertyList: List<Property>){
        var address: Address? = null

        for (property in propertyList){
            if (property.address != "null" || property.city != "null"){

                try {
                    address = getLatLngToAddress(property.address + "," + property.city)
                    if (address != null){
                        val addressToDisplay = LatLng(address.latitude, address.longitude)
                        mMap.addMarker(MarkerOptions().position(addressToDisplay).title(property.id.toString()))
                    }
                }catch (e: java.lang.Exception) {context!!.longToast("Connexion insuffisante pour afficher les propriétées")}

            }
        }
    }

    private fun launchDisplayPropertyFragment(propertyId: Long) {
        var frameLayout: Int = R.id.main_activity_frame
        if (isTablet(context!!)){
            activity!!.main_activity_frame_tablet.visibility = View.VISIBLE
            frameLayout = R.id.main_activity_frame_tablet
        }

        val fragment = DisplayPropertyFragment()
        val bundle = Bundle()
        bundle.putLong("PROPERTY_ID", propertyId)
        fragment.arguments = bundle
        activity!!.supportFragmentManager.beginTransaction()
                .replace(frameLayout, fragment, "findThisFragment")
                .addToBackStack(null)
                .commit()
    }
}