package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    // _binding은 null일 수 있지만, binding은 절대 null이 아님
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // FragmentDetailBinding을 사용하여 레이아웃을 확장합니다.
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments가 null일 경우 기본값("")을 가져옵니다.
        val category = arguments?.getString("category") ?: ""

    }

    private fun getCategoryDescription(category: String): String {
        return when (category) {
            "술집" -> "이 페이지는 술집 관련 정보입니다."
            "카페" -> "이 페이지는 카페 관련 정보입니다."
            "문화" -> "이 페이지는 문화 관련 정보입니다."
            "음식점•식품" -> "이 페이지는 음식점•식품 관련 정보입니다."
            "헬스•뷰티" -> "이 페이지는 헬스•뷰티 관련 정보입니다."
            "교육" -> "이 페이지는 교육 관련 정보입니다."
            "의료•법" -> "이 페이지는 의료•법 관련 정보입니다."
            else -> "전체보기 페이지입니다."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 뷰가 더 이상 사용되지 않도록 _binding을 null로 설정합니다.
        _binding = null
    }
}
