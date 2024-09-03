package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 각 버튼 클릭 시 카테고리별로 DetailActivity로 이동
        binding.totalViewBtn.setOnClickListener { navigateToDetailActivity("전체") }
        binding.barViewBtn.setOnClickListener { navigateToDetailActivity("술집") }
        binding.cafeViewBtn.setOnClickListener { navigateToDetailActivity("카페") }
        binding.cultureViewBtn.setOnClickListener { navigateToDetailActivity("문화") }
        binding.foodViewBtn.setOnClickListener { navigateToDetailActivity("음식점•식품") }
        binding.healthAndBeautyViewBtn.setOnClickListener { navigateToDetailActivity("헬스•뷰티") }
        binding.eduViewBtn.setOnClickListener { navigateToDetailActivity("교육") }
        binding.medicalAndLawViewBtn.setOnClickListener { navigateToDetailActivity("의료•법") }

        // RecyclerView에 LayoutManager 설정
        binding.notice.layoutManager = LinearLayoutManager(requireContext())

        // Firestore에서 공지사항 불러오기
        loadNotices()
    }

    private fun loadNotices() {
        db.collection("noticeinfo")
            .get()
            .addOnSuccessListener { result ->
                val notices = result.map { document ->
                    Notice(
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "" // 내용은 저장만 하고 표시하지 않음
                    )
                }
                binding.notice.adapter = SimpleNoticeAdapter(notices)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun navigateToDetailActivity(category: String) {
        // DetailFragment 이동
        val intent = Intent(requireContext(), DetailFragment::class.java).apply {
            putExtra("category", category)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Notice 데이터 클래스
data class Notice(val title: String, val content: String)

// 간단한 RecyclerView 어댑터
class SimpleNoticeAdapter(private val notices: List<Notice>) :
    RecyclerView.Adapter<SimpleNoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]
        holder.titleTextView.text = notice.title // 제목만 표시
    }

    override fun getItemCount() = notices.size

    class NoticeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(android.R.id.text1)
    }
}