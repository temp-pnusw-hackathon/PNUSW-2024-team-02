package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class DetailFragment : Fragment() {

    private lateinit var gridView: GridView
    private val db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        gridView = view.findViewById(R.id.partnership_gridView)

        // 각 버튼에 대한 클릭 리스너 설정
        val barBtn: ImageButton = view.findViewById(R.id.bar_btn)
        val cafeBtn: ImageButton = view.findViewById(R.id.cafe_btn)
        val cultureBtn: ImageButton = view.findViewById(R.id.culture_btn)
        val foodBtn: ImageButton = view.findViewById(R.id.food_btn)
        val healthBtn: ImageButton = view.findViewById(R.id.health_btn)
        val eduBtn: ImageButton = view.findViewById(R.id.edu_btn)
        val mediBtn: ImageButton = view.findViewById(R.id.medi_btn)
        val totalBtn: ImageButton = view.findViewById(R.id.total_btn)

        barBtn.setOnClickListener { loadPartnershipsByCategory("술집") }
        cafeBtn.setOnClickListener { loadPartnershipsByCategory("카페") }
        cultureBtn.setOnClickListener { loadPartnershipsByCategory("문화") }
        foodBtn.setOnClickListener { loadPartnershipsByCategory("음식점•식품") }
        healthBtn.setOnClickListener { loadPartnershipsByCategory("헬스•뷰티") }
        eduBtn.setOnClickListener { loadPartnershipsByCategory("교육") }
        mediBtn.setOnClickListener { loadPartnershipsByCategory("의료•법") }
        totalBtn.setOnClickListener { loadPartnershipsByCategory("전체") }

        // 권한 확인 및 요청
        checkAndRequestPermissions()

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
                Toast.makeText(requireContext(), "No partnerships found.", Toast.LENGTH_SHORT).show()
            } else {
                val adapter = PartnershipAdapter(requireContext(), documents.documents)
                gridView.adapter = adapter
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
