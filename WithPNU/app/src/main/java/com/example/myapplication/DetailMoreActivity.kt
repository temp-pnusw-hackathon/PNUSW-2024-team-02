package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.RatingBar
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class DetailMoreActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var imageView: ImageView
    private lateinit var dateTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var storeNameTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var mapView: MapView
    private lateinit var writeReview: ImageButton
    private lateinit var starred_number: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var review_list: ListView

    private var latitude: Double? = null
    private var longitude: Double? = null

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var partnershipId: String? = null
    private var photoUrls: List<String> = listOf() // 사진 URL 리스트

    private var storeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_detail)

        imageView = findViewById(R.id.detail_photo)
        dateTextView = findViewById(R.id.partnership_dateTodate)
        contentTextView = findViewById(R.id.partnership_content)
        storeNameTextView = findViewById(R.id.partnership_store)
        titleTextView = findViewById(R.id.store_name)
        mapView = findViewById(R.id.detail_map)
        writeReview = findViewById(R.id.write_review_btn)
        starred_number = findViewById(R.id.starred_number)
        ratingBar = findViewById(R.id.ratingBar)
        review_list = findViewById(R.id.review_list)

        // Intent에서 데이터 수신
        val photoUrl = intent.getStringExtra("photoUrl")
        photoUrls = intent.getStringArrayListExtra("photoUrls") ?: listOf() // PhotoUrls 초기화
        val startDate = intent.getLongExtra("startDate", 0L)
        val endDate = intent.getLongExtra("endDate", 0L)
        val content = intent.getStringExtra("content")
        storeName = intent.getStringExtra("storeName")
        val title = intent.getStringExtra("title")

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        partnershipId = intent.getStringExtra("partnershipId") // partnershipId 초기화


        // 인증된 사용자인지 확인
        if (currentUser == null) {
            Log.e("DetailMoreActivity", "User not authenticated")
            return
        }

        // 사진 설정
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this).load(photoUrl).into(imageView)
        }

        // 날짜 설정
        if (startDate > 0 && endDate > 0) {
            val dateFormat = java.text.SimpleDateFormat("yy.MM.dd", java.util.Locale.getDefault())
            val formattedStartDate = dateFormat.format(java.util.Date(startDate * 1000))
            val formattedEndDate = dateFormat.format(java.util.Date(endDate * 1000))
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

        // 리뷰 쓰기 버튼 클릭

        writeReview.setOnClickListener {
            val intent = Intent(this@DetailMoreActivity, WriteMyReview::class.java)
            intent.putExtra("storeName", storeName)
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }

        // 리뷰 데이터 로드 및 ListView에 표시
        loadReviews()

        // 평균 리뷰 보기
        calculateAndDisplayAverageRating()

    }

    private fun calculateAndDisplayAverageRating() {
        val db = FirebaseFirestore.getInstance()
        if (storeName != null) {
            db.collection("reviews")
                .whereEqualTo("storeName", storeName)
                .get()
                .addOnSuccessListener { documents ->
                    var totalRating = 0.0
                    var reviewCount = 0

                    for (document in documents) {
                        val rating = document.getDouble("rating") ?: 0.0
                        totalRating += rating
                        reviewCount++
                    }

                    if (reviewCount > 0) {
                        val averageRating = totalRating / reviewCount
                        starred_number.text = String.format(Locale.getDefault(), "%.1f", averageRating)
                        ratingBar.rating = averageRating.toFloat() // RatingBar에 평균 평점 설정
                    } else {
                        starred_number.text = "평점 없음"
                        ratingBar.rating = 0f // RatingBar 초기화
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DetailMoreActivity", "Error getting documents: ", e)
                    starred_number.text = "평점 로드 실패"
                    ratingBar.rating = 0f // 실패 시 RatingBar 초기화
                }
        } else {
            starred_number.text = "가게 정보 없음"
            ratingBar.rating = 0f // 가게 정보가 없을 경우 RatingBar 초기화
        }
    }

    private fun loadReviews() {
        if (storeName != null) {
            db.collection("reviews")
                .whereEqualTo("storeName", storeName)
                .get()
                .addOnSuccessListener { documents ->
                    val reviewContents = mutableListOf<String>()

                    for (document in documents) {
                        val content = document.getString("content") ?: "내용 없음"
                        reviewContents.add(content)
                    }

                    // 간단하게 리뷰 내용을 ListView에 표시
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reviewContents)
                    review_list.adapter = adapter
                }
                .addOnFailureListener {
                    Log.w("DetailMoreActivity", "리뷰 데이터를 가져오는 중 오류 발생")
                }
        }
    }


    // MapView 생명주기 관리
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        calculateAndDisplayAverageRating()
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
