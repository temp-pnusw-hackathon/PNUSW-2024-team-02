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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
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

        // 버튼 클릭 리스너 설정
        view.findViewById<ImageButton>(R.id.bar_btn).setOnClickListener { loadPartnershipsByCategory("술집") }
        view.findViewById<ImageButton>(R.id.cafe_btn).setOnClickListener { loadPartnershipsByCategory("카페") }
        view.findViewById<ImageButton>(R.id.culture_btn).setOnClickListener { loadPartnershipsByCategory("문화") }
        view.findViewById<ImageButton>(R.id.food_btn).setOnClickListener { loadPartnershipsByCategory("음식점•식품") }
        view.findViewById<ImageButton>(R.id.health_btn).setOnClickListener { loadPartnershipsByCategory("헬스•뷰티") }
        view.findViewById<ImageButton>(R.id.edu_btn).setOnClickListener { loadPartnershipsByCategory("교육") }
        view.findViewById<ImageButton>(R.id.medi_btn).setOnClickListener { loadPartnershipsByCategory("의료•법") }
        view.findViewById<ImageButton>(R.id.total_btn).setOnClickListener { loadPartnershipsByCategory("전체") }

        // 권한 확인 및 요청
        checkAndRequestPermissions()

        // GridView 클릭 리스너 설정
        gridView.setOnItemClickListener { parent, _, position, _ ->
            val selectedDocument = parent.getItemAtPosition(position) as DocumentSnapshot
            openDetailMoreActivity(selectedDocument)
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
                val adapter = PartnershipAdapter(requireContext(), documents.documents)
                gridView.adapter = adapter
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
