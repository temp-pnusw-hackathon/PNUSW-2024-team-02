package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

class PartnershipNoticeActivity : AppCompatActivity() {

    // Firebase Firestore 인스턴스
    private val db = FirebaseFirestore.getInstance()

    // 날짜를 표시할 EditText
    private lateinit var periodEditText: EditText

    // 위치를 추가할 Button
    private lateinit var addLocationButton: Button

    // 사진 RecyclerView와 어댑터
    private lateinit var photosRecyclerView: RecyclerView
    private val selectedPhotos = mutableListOf<String>() // 선택된 사진 URI 목록
    private val galleryAdapter = GalleryAdapter(selectedPhotos)

    // 시작 날짜와 종료 날짜를 저장할 Calendar 변수
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var selectedLatLng: LatLng? = null // 선택된 위치의 LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_partnership_notice)

        // 시스템 바의 패딩을 설정하여 전체 화면 모드 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // periodEditText 초기화 및 클릭 리스너 설정
        periodEditText = findViewById(R.id.periodEditText)
        periodEditText.setOnClickListener {
            showDateRangePicker()
        }

        // addLocationButton 초기화 및 클릭 리스너 설정
        addLocationButton = findViewById(R.id.addLocationButton)
        addLocationButton.setOnClickListener {
            openMapActivity()
        }

        // 사진 RecyclerView 초기화
        photosRecyclerView = findViewById(R.id.photosRecyclerView)
        photosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        photosRecyclerView.adapter = galleryAdapter

        // 사진 추가 버튼 설정
        val addPhotoButton: ImageButton = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener {
            selectPhotosFromGallery()
        }

        // 업로드 버튼 설정
        val uploadButton: Button = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            uploadDataToFirestore()
        }
    }

    // 날짜 선택 다이얼로그를 표시하는 메서드
    private fun showDateRangePicker() {
        val today = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            startDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            DatePickerDialog(this, { _, endYear, endMonth, endDayOfMonth ->
                endDate = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDayOfMonth)
                }
                updatePeriodEditText()
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()

        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
    }

    // 선택된 두 날짜를 EditText에 표시하는 메서드
    private fun updatePeriodEditText() {
        val dateFormat = SimpleDateFormat("yy/MM/dd", Locale.getDefault())
        if (startDate != null && endDate != null) {
            val startDateString = dateFormat.format(startDate!!.time)
            val endDateString = dateFormat.format(endDate!!.time)
            periodEditText.setText("$startDateString ~ $endDateString")
        }
    }

    // MapActivity를 열어 위치 선택을 처리하는 메서드
    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val placeName = result.data?.getStringExtra("place_name")
            val lat = result.data?.getDoubleExtra("latitude", 0.0)
            val lng = result.data?.getDoubleExtra("longitude", 0.0)
            if (lat != null && lng != null) {
                selectedLatLng = LatLng(lat, lng)
            }
            addLocationButton.text = placeName
        }
    }

    // MapActivity 실행 메서드
    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        mapActivityResultLauncher.launch(intent)
    }

    // 사진 선택을 위한 메서드
    private fun selectPhotosFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        galleryLauncher.launch(intent)
    }

    // ActivityResultLauncher to handle the selected photos
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val clipData = result.data?.clipData
            val data = result.data?.data

            if (clipData != null) { // 여러 이미지 선택 시
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri.toString()
                    selectedPhotos.add(uri)
                }
            } else if (data != null) { // 단일 이미지 선택 시
                val uri = data.toString()
                selectedPhotos.add(uri)
            }
            galleryAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // GalleryAdapter 정의 (사진 클릭 시 삭제)
    inner class GalleryAdapter(private val photoList: MutableList<String>) :
        RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

        inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photoUri = photoList[position]
            Glide.with(holder.photoImageView.context)
                .load(photoUri)
                .into(holder.photoImageView)

            // 사진 클릭 시 삭제
            holder.photoImageView.setOnClickListener {
                photoList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
        }

        override fun getItemCount(): Int = photoList.size
    }

    // Firestore에 데이터를 업로드하는 메서드
    private fun uploadDataToFirestore() {
        val title = findViewById<EditText>(R.id.titleEditText).text.toString()
        val content = findViewById<EditText>(R.id.contentEditText).text.toString()

        // 현재 사용자 정보 가져오기
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (title.isNotEmpty() && content.isNotEmpty() && currentUser != null && selectedLatLng != null) {
            // Firestore에 저장할 데이터 생성
            val data = hashMapOf(
                "title" to title,
                "content" to content,
                "location" to GeoPoint(selectedLatLng!!.latitude, selectedLatLng!!.longitude),
                "postedDate" to Timestamp.now(),
                "uid" to currentUser.uid,
                "photos" to selectedPhotos
            )

            // Firestore에 데이터 저장
            db.collection("partnershipinfo")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "데이터 업로드 성공", Toast.LENGTH_SHORT).show()
                    finish() // 현재 Activity를 종료하여 이전 Fragment로 돌아감
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "데이터 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
