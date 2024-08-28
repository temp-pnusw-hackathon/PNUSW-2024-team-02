package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class DetailMoreActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var dateTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var storeNameTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var mapView: MapView

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_detail)

        imageView = findViewById(R.id.detail_photo)
        dateTextView = findViewById(R.id.partnership_dateTodate)
        contentTextView = findViewById(R.id.partnership_content)
        storeNameTextView = findViewById(R.id.partnership_store)
        titleTextView = findViewById(R.id.store_name)
        mapView = findViewById(R.id.detail_map)

        // Intent에서 데이터 수신
        val photoUrl = intent.getStringExtra("photoUrl")
        val startDate = intent.getLongExtra("startDate", 0L)
        val endDate = intent.getLongExtra("endDate", 0L)
        val content = intent.getStringExtra("content")
        val storeName = intent.getStringExtra("storeName") // storeName 필드 수신
        val title = intent.getStringExtra("title") // title 필드 수신
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        // 로그로 데이터 확인
        Log.d("DetailMoreActivity", "startDate: $startDate")
        Log.d("DetailMoreActivity", "endDate: $endDate")

        // 사진 설정
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this).load(photoUrl).into(imageView)
        }

        // 날짜 설정
        if (startDate > 0 && endDate > 0) {
            val dateFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
            val formattedStartDate = dateFormat.format(Date(startDate * 1000))
            val formattedEndDate = dateFormat.format(Date(endDate * 1000))
            dateTextView.text = "제휴 기간 : $formattedStartDate~$formattedEndDate"
        } else {
            dateTextView.text = "날짜 정보가 없습니다."
        }

        // 제목과 가게 이름 설정
        titleTextView.text = title ?: "업체 이름이 없습니다."
        storeNameTextView.text = "제휴 업체명 : ${storeName ?: "가게 이름이 없습니다."}"

        // 본문 설정
        contentTextView.text = content ?: "내용이 없습니다."

        // 지도 설정
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            val location = LatLng(latitude ?: 0.0, longitude ?: 0.0)
            googleMap.addMarker(MarkerOptions().position(location).title(storeName ?: "제휴 업체 위치"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    // MapView 생명주기 관리
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
