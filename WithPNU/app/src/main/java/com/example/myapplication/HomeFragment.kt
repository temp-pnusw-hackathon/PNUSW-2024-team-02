package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.constraintlayout.helper.widget.Carousel
import androidx.recyclerview.widget.DividerItemDecoration


class HomeFragment : Fragment() {


    //구분선 추가
    class CustomItemDecoration(private val divider: Drawable) : RecyclerView.ItemDecoration() {

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val childCount = parent.childCount
            val itemCount = parent.adapter?.itemCount ?: 0

            for (i in 0 until childCount) {
                // 마지막 아이템이면 구분선을 그리지 않음
                if (i == childCount - 1 && parent.getChildAdapterPosition(parent.getChildAt(i)) == itemCount - 1) {
                    continue
                }

                val child = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams
                val left = parent.paddingLeft
                val right = parent.width - parent.paddingRight
                val top = child.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight

                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            // 마지막 아이템이면 구분선의 간격을 추가하지 않음
            if (position == state.itemCount - 1) {
                outRect.set(0, 0, 0, 0)
            } else {
                outRect.set(0, 0, 0, divider.intrinsicHeight)
            }
        }
    }

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
            binding.totalViewBtn.setImageResource(R.drawable.totalview_click)
            navigateToDetailFragment("전체")
        }
        // "술집" 버튼 클릭 시
        binding.barViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.bar_click)
            navigateToDetailFragment("술집")
        }
        // "카페" 버튼 클릭 시
        binding.cafeViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.cafe_click)
            navigateToDetailFragment("카페")
        }
        // "문화" 버튼 클릭 시
        binding.cultureViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.culture_click)
            navigateToDetailFragment("문화")
        }
        // "음식점•식품" 버튼 클릭 시
        binding.foodViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.food_click)
            navigateToDetailFragment("음식점•식품")
        }
        // "헬스•뷰티" 버튼 클릭 시
        binding.healthAndBeautyViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.health_and_beauty_click)
            navigateToDetailFragment("헬스•뷰티")
        }
        // "교육" 버튼 클릭 시
        binding.eduViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.edu_click)
            navigateToDetailFragment("교육")
        }
        // "의료•법" 버튼 클릭 시
        binding.medicalAndLawViewBtn.setOnClickListener {
            binding.totalViewBtn.setImageResource(R.drawable.medical_and_law_click)
            navigateToDetailFragment("의료•법")
        }

        // RecyclerView에 LayoutManager 설정
        binding.notice.layoutManager = LinearLayoutManager(requireContext())
        // RecyclerView에 Custom 구분선 추가
        binding.notice.addItemDecoration(CustomItemDecoration(resources.getDrawable(R.drawable.custom_divider, null)))

        // Firestore에서 공지사항 불러오기
        loadNotices()

        // Carousel 설정 추가
        setupCarousel()
    }

    // Carousel 설정 메서드 추가
    private fun setupCarousel() {
        val carousel = binding.carousel // XML에서 Carousel 참조
        carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return 4 // Carousel 아이템 수
            }

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

            override fun onNewItem(index: Int) {
            }
        })
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

        // MainNavigationbar 액티비티의 replaceFragment 메소드를 호출
        (activity as? MainNavigationbar)?.replaceFragment(detailFragment)
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
