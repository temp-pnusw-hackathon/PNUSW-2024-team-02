package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
            navigateToDetailActivity("")
        }
        // "술집" 버튼 클릭 시
        binding.barViewBtn.setOnClickListener {
            navigateToDetailActivity("술집")
        }
        // "카페" 버튼 클릭 시
        binding.cafeViewBtn.setOnClickListener {
            navigateToDetailActivity("카페")
        }
        // "문화" 버튼 클릭 시
        binding.cultureViewBtn.setOnClickListener {
            navigateToDetailActivity("문화")
        }
        // "음식점•식품" 버튼 클릭 시
        binding.foodViewBtn.setOnClickListener {
            navigateToDetailActivity("음식점•식품")
        }
        // "헬스•뷰티" 버튼 클릭 시
        binding.healthAndBeautyViewBtn.setOnClickListener {
            navigateToDetailActivity("헬스•뷰티")
        }
        // "교육" 버튼 클릭 시
        binding.eduViewBtn.setOnClickListener {
            navigateToDetailActivity("교육")
        }
        // "의료•법" 버튼 클릭 시
        binding.medicalAndLawViewBtn.setOnClickListener {
            navigateToDetailActivity("의료•법")
        }
    }

    private fun navigateToDetailActivity(category: String) {
        val intent = Intent(activity, DetailFragment::class.java).apply {
            putExtra("category", category)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
