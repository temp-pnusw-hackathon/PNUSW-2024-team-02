package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MypageAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MypageAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mypage_admin, container, false)

        // partnership_notice_btn 클릭 이벤트 처리
        val partnershipNoticeButton: TextView = view.findViewById(R.id.partnership_notice_btn)
        partnershipNoticeButton.setOnClickListener {
            // PartnershipNoticeActivity로 이동
            val intent = Intent(activity, PartnershipNoticeActivity::class.java)
            startActivity(intent)
        }

        val editNoticeButton: TextView = view.findViewById(R.id.edit_partnership_notice_btn)
        editNoticeButton.setOnClickListener{
            val intent = Intent(activity, EditPartnershipNoticeActivity::class.java)
            startActivity(intent)
        }

        // 공지사항 작성하기 버튼 클릭
        val uploadNoticeButton: TextView = view.findViewById(R.id.upload_notice_btn)
        uploadNoticeButton.setOnClickListener {
            //UploadNoticeActivity로 이동
            val intent = Intent(activity, UploadNoticeActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭
        val logoutButton: TextView = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val logoutIntent = Intent(activity, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(logoutIntent)
        }


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MypageAdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MypageAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
