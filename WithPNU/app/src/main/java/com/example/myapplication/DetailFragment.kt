package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var gridView: GridView
    private lateinit var ongoingSwitch: Switch
    private var selectedCategory: String = "전체"  // 선택된 카테고리를 저장하는 변수
    private val db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        gridView = view.findViewById(R.id.partnership_gridView)
        ongoingSwitch = view.findViewById(R.id.ongoing_switch)

        // 버튼 클릭 리스너 설정
        view.findViewById<ImageButton>(R.id.bar_btn).setOnClickListener {
            selectedCategory = "술집"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.cafe_btn).setOnClickListener {
            selectedCategory = "카페"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.culture_btn).setOnClickListener {
            selectedCategory = "문화"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.food_btn).setOnClickListener {
            selectedCategory = "음식점•식품"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.health_btn).setOnClickListener {
            selectedCategory = "헬스•뷰티"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.edu_btn).setOnClickListener {
            selectedCategory = "교육"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.medi_btn).setOnClickListener {
            selectedCategory = "의료•법"
            loadPartnershipsByCategory(selectedCategory)
        }
        view.findViewById<ImageButton>(R.id.total_btn).setOnClickListener {
            selectedCategory = "전체"
            loadPartnershipsByCategory(selectedCategory)
        }

        // 권한 확인 및 요청
        checkAndRequestPermissions()

        // GridView 클릭 리스너 설정
        gridView.setOnItemClickListener { parent, _, position, _ ->
            val selectedDocument = parent.getItemAtPosition(position) as DocumentSnapshot
            openDetailMoreActivity(selectedDocument)
        }

        // Switch 상태 변경 리스너 설정
        ongoingSwitch.setOnCheckedChangeListener { _, isChecked ->
            loadPartnershipsByCategory(selectedCategory)  // 현재 선택된 카테고리를 다시 로드
        }

        return view
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        } else {
            // 권한이 이미 부여되어 있으면 데이터를 로드
            loadPartnershipsByCategory("전체")
        }
    }

    // Firestore에서 데이터를 로드하고 GridView에 표시하는 함수
    private fun loadPartnershipsByCategory(category: String) {
        val query = if (category == "전체") {
            db.collection("partnershipinfo")
        } else {
            db.collection("partnershipinfo").whereEqualTo("category", category)
        }

        query.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // 데이터가 없을 경우 빈 어댑터 설정
                gridView.adapter = null
            } else {
                val filteredDocuments = if (ongoingSwitch.isChecked) {
                    val today = Date()
                    documents.documents.filter { document ->
                        val startDate = document.getTimestamp("startDate")?.toDate()
                        val endDate = document.getTimestamp("endDate")?.toDate()
                        startDate != null && endDate != null && today in startDate..endDate
                    }
                } else {
                    documents.documents
                }

                if (filteredDocuments.isEmpty()) {
                    gridView.adapter = null
                } else {
                    val adapter = PartnershipAdapter(requireContext(), filteredDocuments)
                    gridView.adapter = adapter
                }
            }
        }.addOnFailureListener { exception ->
            // 에러가 발생했을 때도 빈 어댑터 설정
            gridView.adapter = null
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 선택된 항목의 데이터를 DetailMoreActivity로 전달하는 함수
    private fun openDetailMoreActivity(document: DocumentSnapshot) {
        // photos 필드를 안전하게 가져오고, 올바른 형식인지 확인
        val photos = document.get("photos") as? List<*>
        val firstPhotoUrl = photos?.filterIsInstance<String>()?.firstOrNull()

        // Null 또는 다른 타입이 섞여 있을 가능성에 대비
        if (firstPhotoUrl == null) {
            Log.e("DetailFragment", "No valid photo URL found.")
        }

        val intent = Intent(requireContext(), DetailMoreActivity::class.java).apply {
            putExtra("photoUrl", firstPhotoUrl)
            putExtra("startDate", document.getTimestamp("startDate")?.seconds ?: 0L)
            putExtra("endDate", document.getTimestamp("endDate")?.seconds ?: 0L)
            putExtra("content", document.getString("content"))
            putExtra("storeName", document.getString("storeName")) // storeName 추가
            putExtra("title", document.getString("title")) // title 추가
            putExtra("latitude", document.getGeoPoint("location")?.latitude)
            putExtra("longitude", document.getGeoPoint("location")?.longitude)
        }
        startActivity(intent)
    }
}
