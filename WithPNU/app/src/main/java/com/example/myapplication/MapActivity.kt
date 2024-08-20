package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

private const val AUTOCOMPLETE_REQUEST_CODE = 1

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var selectedPlaceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Places SDK 초기화
        Places.initialize(applicationContext, "AIzaSyCqC4aLoGhe6_Mv8KbFyD6rARFq9OsZ5EE")
        placesClient = Places.createClient(this)

        // MapFragment 설정
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 장소 검색 버튼 설정
        findViewById<Button>(R.id.searchButton).setOnClickListener {
            openAutocompleteActivity()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 초기 위치 설정 (예: 부산대학교 근처)
        val initialLocation = LatLng(35.231554, 129.084139)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))

        // 마커 클릭 리스너 설정
        mMap.setOnMarkerClickListener { marker ->
            marker.title?.let {
                // 선택된 장소 이름을 저장하고 PartnershipNoticeActivity로 전달
                selectedPlaceName = it
                returnToPartnershipNoticeActivity()
            }
            true
        }
    }

    // AutocompleteActivity를 열어 장소 검색 기능 제공
    private fun openAutocompleteActivity() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val latLng = place.latLng
            if (latLng != null) {
                mMap.clear()
                val marker = mMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                marker?.showInfoWindow()  // 마커를 클릭한 것처럼 정보 창을 표시
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            status.statusMessage?.let { errorMessage ->
                // 오류 처리 (예: 사용자에게 오류 메시지 표시)
                println("Error: $errorMessage")
            }
        }
    }

    // Places API를 사용해 장소 이름을 가져오는 메서드
    private fun fetchPlaceName(latLng: LatLng) {
        val placeFields = listOf(Place.Field.NAME)
        val request = FetchPlaceRequest.newInstance(latLng.toString(), placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place: Place = response.place
            val placeName = place.name

            // 장소 이름을 결과로 반환 (사용자가 필요로 할 경우)
            val resultIntent = Intent().apply {
                putExtra("place_name", placeName)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            // 오류 처리 (예: 로그 출력)
        }
    }

    // 선택된 장소 이름을 PartnershipNoticeActivity로 전달하고 종료
    private fun returnToPartnershipNoticeActivity() {
        val resultIntent = Intent().apply {
            putExtra("place_name", selectedPlaceName)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
