package com.example.myapplication
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    // Firebase Firestore 인스턴스 생성
    private val db = FirebaseFirestore.getInstance()
    // UI 요소 선언
    private lateinit var periodEditText: EditText
    private lateinit var addLocationButton: Button
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var spinnerCategories: Spinner
    private val selectedPhotos = mutableListOf<String>() // 선택된 사진 URI 목록
    private val galleryAdapter = GalleryAdapter(selectedPhotos)

    // 날짜 관련 변수 선언
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    // 위치 관련 변수 선언
    private var selectedLatLng: LatLng? = null

    // 선택된 카테고리를 저장할 변수 선언
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_partnership_notice)

        // UI 요소 초기화 및 이벤트 리스너 설정
        periodEditText = findViewById(R.id.periodEditText)
        periodEditText.setOnClickListener {
            showDateRangePicker() // 날짜 범위 선택 다이얼로그 표시
        }
        addLocationButton = findViewById(R.id.addLocationButton)
        addLocationButton.setOnClickListener {
            openMapActivity() // 지도 화면 열기
        }
        photosRecyclerView = findViewById(R.id.photosRecyclerView)
        photosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        photosRecyclerView.adapter = galleryAdapter
        val addPhotoButton: ImageButton = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener {
            selectPhotosFromGallery() // 갤러리에서 사진 선택
        }
        val uploadButton: Button = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            uploadDataToFirestore() // Firestore에 데이터 업로드
        }

        // Spinner 초기화 및 선택 이벤트 설정
        setupCategoriesSpinner()

        // 상단 툴바 추가하기
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 추가
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "제휴공지 작성하기" // 제목 달기
    }

    // 카테고리 선택 함수
    private fun setupCategoriesSpinner() {
        // 1. Spinner 참조 얻기
        spinnerCategories = findViewById(R.id.spinner_categories) // 여기서 R.id.categories_spinner는 Spinner의 ID입니다.

        // 2. 카테고리 데이터 정의
        val categories = arrayOf("전체", "술집", "카페", "문화", "음식점•식품", "헬스•뷰티", "교육", "의료•법")
        // 3. ArrayAdapter 생성
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        // 4. 어댑터의 드롭다운 레이아웃 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 5. Spinner에 어댑터 설정
        spinnerCategories.adapter = adapter

        // 6. 선택 이벤트 리스너 설정 (선택한 항목을 처리할 수 있습니다.)
        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 선택된 항목의 위치에 따라 행동을 정의할 수 있습니다.
                selectedCategory = categories[position] // 선택된 카테고리를 변수에 저장
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무 항목도 선택되지 않았을 때의 동작을 정의합니다.
            }
        }
    }

    // 날짜 선택 다이얼로그를 표시하는 메서드
    private fun showDateRangePicker() {
        val today = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // 시작 날짜 선택
            startDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                clearTime() // 시간 정보를 제거하여 년/월/일만 저장
            }
            DatePickerDialog(this, { _, endYear, endMonth, endDayOfMonth ->
                // 종료 날짜 선택
                endDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, endYear)
                    set(Calendar.MONTH, endMonth)
                    set(Calendar.DAY_OF_MONTH, endDayOfMonth)
                    clearTime() // 시간 정보를 제거하여 년/월/일만 저장
                }
                updatePeriodEditText() // 선택된 날짜를 EditText에 표시
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
    }
    // Calendar 객체에서 시간 정보를 제거하는 확장 함수
    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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
            addLocationButton.text = placeName // 선택된 위치 이름을 버튼에 표시
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

        // 모든 필드가 올바르게 채워졌는지 확인
        if (title.isNotEmpty() && content.isNotEmpty() && currentUser != null && selectedLatLng != null && startDate != null && endDate != null && selectedCategory != null) {
            // Firestore에 저장할 데이터 생성
            val data = hashMapOf(
                "title" to title,
                "content" to content,
                "location" to GeoPoint(selectedLatLng!!.latitude, selectedLatLng!!.longitude),
                "postedDate" to Timestamp.now(),
                "uid" to currentUser.uid,
                "photos" to selectedPhotos,
                "startDate" to Timestamp(startDate!!.time), // 년/월/일만 저장된 시작 날짜
                "endDate" to Timestamp(endDate!!.time), // 년/월/일만 저장된 종료 날짜
                "category" to selectedCategory // 선택된 카테고리를 Firestore에 저장
            )

            // Firestore에 데이터 저장
            db.collection("partnershipinfo")
                .add(data)
                .addOnSuccessListener {
                    // 업로드 성공 시 Toast 메시지와 함께 Activity 종료
                    Toast.makeText(this, "데이터 업로드 성공", Toast.LENGTH_SHORT).show()
                    finish() // 현재 Activity를 종료하여 이전 Fragment로 돌아감
                }
                .addOnFailureListener { e ->
                    // 업로드 실패 시 Toast 메시지 출력
                    Toast.makeText(this, "데이터 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 필드가 비어 있는 경우 경고 메시지 출력
            Toast.makeText(this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // 뒤로가기 버튼 눌렀을 때 액티비티 종료
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}