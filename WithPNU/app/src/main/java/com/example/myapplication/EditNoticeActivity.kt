package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class EditNoticeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var noticeGridView: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_notice)

        noticeGridView = findViewById(R.id.notice_gridView)

        // 현재 사용자가 올린 공지사항들을 GridView에 표시
        loadUserNotices()

        // GridView의 항목을 클릭하면 MoreEditNoticeActivity로 이동
        noticeGridView.setOnItemClickListener { parent, _, position, _ ->
            val selectedDocument = parent.getItemAtPosition(position) as DocumentSnapshot
            openEditNoticeActivity(selectedDocument)
        }

        // 상단 툴바 추가하기
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 추가
        supportActionBar?.setDisplayShowHomeEnabled(true)


        supportActionBar?.title = "공지사항 관리하기" // 제목 설정

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // 뒤로가기 버튼 눌렀을 때 액티비티 종료
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserNotices() {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("noticeinfo")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        noticeGridView.adapter = null
                    } else {
                        val adapter = NoticeAdapter(this, documents.documents)
                        noticeGridView.adapter = adapter
                    }
                }
                .addOnFailureListener { exception ->
                    noticeGridView.adapter = null
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("EditNoticeActivity", "Current user is null")
        }
    }

    private fun openEditNoticeActivity(document: DocumentSnapshot) {
        val intent = Intent(this, MoreEditNoticeActivity::class.java).apply {
            putExtra("documentId", document.id)
            putExtra("title", document.getString("title") ?: "")
            putExtra("content", document.getString("content") ?: "")
            // 필요한 다른 데이터를 추가할 수 있습니다.
        }
        startActivity(intent)
    }
}
