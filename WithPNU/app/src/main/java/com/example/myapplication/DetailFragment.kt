package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
    private var selectedCategory: String = "전체"
    private val db = FirebaseFirestore.getInstance()
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001

    private val buttonState = mutableMapOf(
        "전체" to false,
        "술집" to false,
        "카페" to false,
        "문화" to false,
        "음식점•식품" to false,
        "헬스•뷰티" to false,
        "교육" to false,
        "의료•법" to false
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)
        gridView = view.findViewById(R.id.partnership_gridView)
        ongoingSwitch = view.findViewById(R.id.ongoing_switch)

        arguments?.let {
            selectedCategory = it.getString("selectedCategory", "전체")
        } ?: run { selectedCategory = "전체" }

        updateButtonState(view, selectedCategory)
        loadPartnershipsByCategory(selectedCategory)

        setButtonListeners(view)

        checkAndRequestPermissions()
        gridView.setOnItemClickListener { parent, _, position, _ ->
            val selectedDocument = parent.getItemAtPosition(position) as DocumentSnapshot
            openDetailMoreActivity(selectedDocument)
        }

        ongoingSwitch.setOnCheckedChangeListener { _, _ ->
            loadPartnershipsByCategory(selectedCategory)
        }

        return view
    }

    private fun setButtonListeners(view: View) {
        val buttons = listOf(
            R.id.total_btn to "전체",
            R.id.bar_btn to "술집",
            R.id.cafe_btn to "카페",
            R.id.culture_btn to "문화",
            R.id.food_btn to "음식점•식품",
            R.id.health_btn to "헬스•뷰티",
            R.id.edu_btn to "교육",
            R.id.medi_btn to "의료•법"
        )

        buttons.forEach { (btnId, category) ->
            view.findViewById<ImageButton>(btnId).setOnClickListener {
                updateButtonState(view, category)
                loadPartnershipsByCategory(category)
            }
        }
    }

    private fun updateButtonState(view: View, category: String) {
        buttonState.keys.forEach { key -> buttonState[key] = false }
        buttonState[category] = true
        val imageMap = mapOf(
            "전체" to R.drawable.big_ttl_click,
            "술집" to R.drawable.big_bar_click,
            "카페" to R.drawable.big_cafe_click,
            "문화" to R.drawable.big_culture_click,
            "음식점•식품" to R.drawable.big_food_click,
            "헬스•뷰티" to R.drawable.big_healthandbeauty_click,
            "교육" to R.drawable.big_edu_click,
            "의료•법" to R.drawable.big_medicalandlaw_click
        )

        view.findViewById<ImageButton>(R.id.total_btn).setImageResource(if (buttonState["전체"] == true) imageMap["전체"]!! else R.drawable.totalview)
        view.findViewById<ImageButton>(R.id.bar_btn).setImageResource(if (buttonState["술집"] == true) imageMap["술집"]!! else R.drawable.bar)
        view.findViewById<ImageButton>(R.id.cafe_btn).setImageResource(if (buttonState["카페"] == true) imageMap["카페"]!! else R.drawable.cafe)
        view.findViewById<ImageButton>(R.id.culture_btn).setImageResource(if (buttonState["문화"] == true) imageMap["문화"]!! else R.drawable.culture)
        view.findViewById<ImageButton>(R.id.food_btn).setImageResource(if (buttonState["음식점•식품"] == true) imageMap["음식점•식품"]!! else R.drawable.food)
        view.findViewById<ImageButton>(R.id.health_btn).setImageResource(if (buttonState["헬스•뷰티"] == true) imageMap["헬스•뷰티"]!! else R.drawable.health_and_beauty)
        view.findViewById<ImageButton>(R.id.edu_btn).setImageResource(if (buttonState["교육"] == true) imageMap["교육"]!! else R.drawable.edu)
        view.findViewById<ImageButton>(R.id.medi_btn).setImageResource(if (buttonState["의료•법"] == true) imageMap["의료•법"]!! else R.drawable.medical_and_law)
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        } else {
            loadPartnershipsByCategory("전체")
        }
    }

    private fun loadPartnershipsByCategory(category: String) {
        val partnershipQuery = if (category == "전체") db.collection("partnershipinfo")
        else db.collection("partnershipinfo").whereEqualTo("category", category)

        val crawledQuery = if (category == "전체") db.collection("crawled_data")
        else db.collection("crawled_data").whereEqualTo("category", category)

        partnershipQuery.get().addOnSuccessListener { partnershipDocuments ->
            crawledQuery.get().addOnSuccessListener { crawledDocuments ->
                val allDocuments = (partnershipDocuments.documents + crawledDocuments.documents).filter {
                    ongoingSwitch.isChecked.not() || isOngoing(it)
                }

                gridView.adapter = if (allDocuments.isEmpty()) null else PartnershipAdapter(requireContext(), allDocuments)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOngoing(document: DocumentSnapshot): Boolean {
        val today = Date()
        val startDate = document.getTimestamp("startDate")?.toDate()
        val endDate = document.getTimestamp("endDate")?.toDate()
        return startDate != null && endDate != null && today in startDate..endDate
    }

    private fun openDetailMoreActivity(document: DocumentSnapshot) {
        val intent = Intent(requireContext(), DetailMoreActivity::class.java).apply {
            val photos = document.get("photos") as? List<*>
            val photoUrls = photos?.filterIsInstance<String>() ?: emptyList()
            putExtra("photoUrls", ArrayList(photoUrls))
            putExtra("startDate", document.getTimestamp("startDate")?.seconds ?: 0L)
            putExtra("endDate", document.getTimestamp("endDate")?.seconds ?: 0L)
            putExtra("content", document.getString("content"))
            putExtra("storeName", document.getString("storeName"))
            putExtra("title", document.getString("title"))
            putExtra("latitude", document.getGeoPoint("location")?.latitude)
            putExtra("longitude", document.getGeoPoint("location")?.longitude)
        }
        startActivity(intent)
    }
}