package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        // "전체보기" 버튼 클릭 시
        binding.totalViewBtn.setOnClickListener {
            navigateToDetailFragment("전체")
        }
        // "술집" 버튼 클릭 시
        binding.barViewBtn.setOnClickListener {
            navigateToDetailFragment("술집")
        }
        // "카페" 버튼 클릭 시
        binding.cafeViewBtn.setOnClickListener {
            navigateToDetailFragment("카페")
        }
        // "문화" 버튼 클릭 시
        binding.cultureViewBtn.setOnClickListener {
            navigateToDetailFragment("문화")
        }
        // "음식점•식품" 버튼 클릭 시
        binding.foodViewBtn.setOnClickListener {
            navigateToDetailFragment("음식점•식품")
        }
        // "헬스•뷰티" 버튼 클릭 시
        binding.healthAndBeautyViewBtn.setOnClickListener {
            navigateToDetailFragment("헬스•뷰티")
        }
        // "교육" 버튼 클릭 시
        binding.eduViewBtn.setOnClickListener {
            navigateToDetailFragment("교육")
        }
        // "의료•법" 버튼 클릭 시
        binding.medicalAndLawViewBtn.setOnClickListener {
            navigateToDetailFragment("의료•법")
        }

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

    private fun navigateToDetailFragment(category: String) {
        val detailFragment = DetailFragment().apply {
            arguments = Bundle().apply {
                putString("selectedCategory", category)
            }
        }

        // MainNavigationbar 액티비티의 replaceFragment 메소드를 호출하고, Navigation Bar의 상태를 업데이트
        (activity as? MainNavigationbar)?.replaceFragment(detailFragment, R.id.detail)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Notice 데이터 클래스
data class Notice(val title: String, val content: String) // content는 사용하지 않음

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
        val titleTextView: TextView = view.findViewById(android.R.id.text1) // 제목만 표시할 TextView
    }
}
