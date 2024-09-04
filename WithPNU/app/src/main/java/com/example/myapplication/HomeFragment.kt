package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.constraintlayout.helper.widget.Carousel


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

        // 각 버튼 클릭 시 동작
        setupCategoryButtons()

        // RecyclerView에 LayoutManager 설정
        binding.notice.layoutManager = LinearLayoutManager(requireContext())

        // Firestore에서 공지사항 불러오기 및 아이템 클릭 이벤트 처리
        loadNotices { notice ->
            val intent = Intent(requireContext(), ViewMoreNoticeActivity::class.java)
            intent.putExtra("noticeId", notice.id)  // Notice ID 전달
            intent.putExtra("userId", notice.userId)  // 업로드한 사용자 ID 전달
            startActivity(intent)
        }

        // Carousel 설정 추가
        setupCarousel()
    }

    private fun setupCategoryButtons() {
        binding.totalViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.totalview_click)
            navigateToDetailFragment("전체")
        }
        binding.barViewBtn.setOnClickListener {
            binding.barViewBtn.setImageResource(R.drawable.bar_click)
            navigateToDetailFragment("술집")
        }
        binding.cafeViewBtn.setOnClickListener {
            binding.cafeViewBtn.setImageResource(R.drawable.cafe_click)
            navigateToDetailFragment("카페")
        }
        binding.cultureViewBtn.setOnClickListener {
            binding.cultureViewBtn.setImageResource(R.drawable.culture_click)
            navigateToDetailFragment("문화")
        }
        binding.foodViewBtn.setOnClickListener {
            binding.foodViewBtn.setImageResource(R.drawable.food_click)
            navigateToDetailFragment("음식점•식품")
        }
        binding.healthAndBeautyViewBtn.setOnClickListener {
            binding.healthAndBeautyViewBtn.setImageResource(R.drawable.health_and_beauty_click)
            navigateToDetailFragment("헬스•뷰티")
        }
        binding.eduViewBtn.setOnClickListener {
            binding.eduViewBtn.setImageResource(R.drawable.edu_click)
            navigateToDetailFragment("교육")
        }
        binding.medicalAndLawViewBtn.setOnClickListener {
            binding.medicalAndLawViewBtn.setImageResource(R.drawable.medical_and_law_click)
            navigateToDetailFragment("의료•법")
        }
    }

    private fun setupCarousel() {
        val carousel = binding.carousel // XML에서 Carousel 참조
        carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int = 4 // Carousel 아이템 수

            override fun populate(view: View, index: Int) {
                val imageView = view as ImageView
                when (index) {
                    0 -> imageView.setImageResource(R.drawable.skyeye)
                    1 -> imageView.setImageResource(R.drawable.megabox)
                    2 -> imageView.setImageResource(R.drawable.os_fit)
                    3 -> imageView.setImageResource(R.drawable.credit_burger)
                    else -> imageView.setImageResource(R.drawable.skyeye)
                }
            }

            override fun onNewItem(index: Int) {}
        })
    }

    private fun loadNotices(onItemClick: (Notice) -> Unit) {
        db.collection("noticeinfo")
            .get()
            .addOnSuccessListener { result ->
                val notices = result.map { document ->
                    Notice(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "",
                        userId = document.getString("userId") ?: ""  // userId 필드 추가
                    )
                }
                binding.notice.adapter = SimpleNoticeAdapter(notices, onItemClick)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "공지사항을 불러오는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDetailFragment(category: String) {
        val detailFragment = DetailFragment().apply {
            arguments = Bundle().apply {
                putString("selectedCategory", category)
            }
        }

        (activity as? MainNavigationbar)?.replaceFragment(detailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Notice 데이터 클래스
data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val userId: String
)

// RecyclerView 어댑터
class SimpleNoticeAdapter(
    private val notices: List<Notice>,
    private val onItemClick: (Notice) -> Unit
) : RecyclerView.Adapter<SimpleNoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]
        holder.titleTextView.text = notice.title
        holder.itemView.setOnClickListener {
            onItemClick(notice)
        }
    }

    override fun getItemCount() = notices.size

    class NoticeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(android.R.id.text1)
    }
}

