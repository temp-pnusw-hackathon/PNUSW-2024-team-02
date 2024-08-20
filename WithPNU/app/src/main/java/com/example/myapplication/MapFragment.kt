package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapFragment : Fragment(), OnMapReadyCallback {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient

    private val defaultLocation = LatLng(35.231, 129.083) // 부산대 위치

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // Places API 초기화
        Places.initialize(requireContext(), "YOUR_API_KEY_HERE")
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        // 지도 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            updateMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f)) // 기본 위치를 부산대로 설정
        updateMap()
    }

    private fun updateMap() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        fetchNearbyPlaces(currentLatLng) // 현재 위치 기반으로 가게 데이터 가져오기
                    }
                }
            } else {
                showLocationSettingDialog()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fetchNearbyPlaces(latLng: LatLng) {
        // 권한 확인
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // 장소 정보 요청
        placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
            for (placeLikelihood in response.placeLikelihoods) {
                val place = placeLikelihood.place
                val placeLatLng = place.latLng
                if (placeLatLng != null) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(placeLatLng)
                            .title(place.name)
                    )
                }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            // 오류 처리 (예: 로그 출력)
        }
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationSettingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("위치 서비스 비활성화")
            .setMessage("위치 서비스를 활성화해야 합니다. 위치 설정 화면으로 이동하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
