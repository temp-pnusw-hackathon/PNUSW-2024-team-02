package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoreEditNoticeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_edit_notice)

        // 상단 툴바 추가
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 추가
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "공지사항 수정하기" // 제목 설정

        // EditText 초기화
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)

        // 인텐트로 전달된 데이터 로드
        loadIntentData()

        // 업로드 버튼 클릭 리스너
        findViewById<Button>(R.id.uploadButton).setOnClickListener {
            if (documentId != null) {
                updateDataInFirestore()
            } else {
                Toast.makeText(this, "문서 ID가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 인텐트로 전달된 공지사항 데이터를 로드하여 화면에 표시
     */
    private fun loadIntentData() {
        documentId = intent.getStringExtra("documentId")
        titleEditText.setText(intent.getStringExtra("title") ?: "")
        contentEditText.setText(intent.getStringExtra("content") ?: "")
    }

    /**
     * Firestore에 있는 데이터를 업데이트
     */
    private fun updateDataInFirestore() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val data = hashMapOf(
                    "title" to title,
                    "content" to content,
                    "uid" to currentUser.uid
                )

                documentId?.let { id ->
                    db.collection("noticeinfo").document(id)
                        .set(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "공지사항 수정 성공", Toast.LENGTH_SHORT).show()
                            finish() // 수정 후 액티비티 종료
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "공지사항 수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // 뒤로가기 버튼 눌렀을 때 액티비티 종료
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
