package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore

class total_notice : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var gridView: GridView
    private lateinit var noticeList: ArrayList<Notice>
    private lateinit var adapter: NoticeGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_notice)

        gridView = findViewById(R.id.partnership_gridView)  // Make sure this ID exists in your layout
        noticeList = ArrayList()

        loadNotices()

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedNotice = noticeList[position]
            val intent = Intent(this, ViewMoreNotice::class.java)
            intent.putExtra("noticeTitle", selectedNotice.title)
            intent.putExtra("userId", selectedNotice.userId)
            startActivity(intent)
        }

        // 상단 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "공지사항 자세히 보기"
    }

    private fun loadNotices() {
        db.collection("noticeinfo")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val notice = Notice(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "",
                        userId = document.getString("uid") ?: ""
                    )
                    noticeList.add(notice)
                }
                adapter = NoticeGridAdapter(this, noticeList)
                gridView.adapter = adapter
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to load notices", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
class NoticeGridAdapter(
    private val context: Context,
    private val notices: ArrayList<Notice>
) : BaseAdapter() {

    override fun getCount(): Int = notices.size

    override fun getItem(position: Int): Any = notices[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val titleTextView: TextView = view.findViewById(android.R.id.text1)

        val notice = notices[position]
        titleTextView.text = notice.title

        return view
    }
}
