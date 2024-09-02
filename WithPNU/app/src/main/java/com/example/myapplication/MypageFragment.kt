package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MypageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MypageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Firestore와 FirebaseAuth 인스턴스
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // View 요소를 나중에 초기화할 변수
    private lateinit var mypageNickname: TextView
    private lateinit var userMajorInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // View 요소 초기화
        mypageNickname = view.findViewById(R.id.mypage_nickname)
        userMajorInfo = view.findViewById(R.id.userMajorInfo)

        // Firestore에서 사용자 정보 가져오기
        fetchUserInfo()

        // 로그아웃 버튼 클릭
        val logoutButton: TextView = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val logoutIntent = Intent(activity, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(logoutIntent)
        }

        // 내 정보 수정하기 버튼 클릭
        val editProfileButton: TextView = view.findViewById(R.id.edit_profile_btn)
        editProfileButton.setOnClickListener {
            val intent = Intent(activity, ChangeMyinfoActivity::class.java)
            startActivity(intent)
        }

        // 내가 작성한 리뷰 보기 버튼 클릭
        val viewReviewButton: TextView = view.findViewById(R.id.view_review_btn)
        viewReviewButton.setOnClickListener {
            val intent = Intent(activity, ViewMyReview::class.java)
            startActivity(intent)
        }

        return view
    }

    /**
     * Firestore에서 현재 사용자의 정보를 가져와서 화면에 표시하는 함수
     */
    private fun fetchUserInfo() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Firestore에서 사용자 문서를 가져옴
                    val userDoc = db.collection("Users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val username = userDoc.getString("username") ?: "닉네임 없음"
                        val department = userDoc.getString("department") ?: "학과 없음"

                        // 화면에 표시
                        mypageNickname.text = username
                        userMajorInfo.text = department
                    } else {
                        mypageNickname.text = "닉네임 없음"
                        userMajorInfo.text = "학과 없음"
                    }
                } catch (e: Exception) {
                    mypageNickname.text = "오류 발생"
                    userMajorInfo.text = "오류 발생"
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MypageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MypageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
