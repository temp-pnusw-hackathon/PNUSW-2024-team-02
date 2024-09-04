package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ViewMoreNotice : AppCompatActivity() {

    private lateinit var noticeTitleTxt: TextView
    private lateinit var noticeCouncilTxt: TextView
    private lateinit var noticeContentTxt: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_more_notice)

        // View 초기화
        noticeTitleTxt = findViewById(R.id.notice_title_txt)
        noticeCouncilTxt = findViewById(R.id.notice_council_txt)
        noticeContentTxt = findViewById(R.id.notice_content_txt)  // 초기화 누락된 부분 추가

        // Intent로부터 전달받은 noticeId와 userId를 가져오기
        val noticeId = intent.getStringExtra("noticeId") ?: return
        val userId = intent.getStringExtra("userId") ?: return

        // 공지사항 내용 불러오기
        loadNoticeDetails(noticeId, userId)
    }

    private fun loadNoticeDetails(noticeId: String, userId: String) {
        db.collection("noticeinfo").document(noticeId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    noticeTitleTxt.text = document.getString("title")
                    noticeContentTxt.text = document.getString("content")

                    // 작성자 이름 불러오기
                    loadUsername(userId)
                } else {
                    Toast.makeText(this, "공지사항을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "공지사항을 불러오는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUsername(userId: String) {
        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // "userId" 대신 "id" 필드명을 사용
                    noticeCouncilTxt.text = document.getString("username")
                } else {
                    Toast.makeText(this, "작성자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "작성자 정보를 불러오는 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

}
