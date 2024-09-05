package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot

class NoticeAdapter(
    private val context: Context,
    private val notices: List<DocumentSnapshot>
) : BaseAdapter() {

    override fun getCount(): Int {
        return notices.size
    }

    override fun getItem(position: Int): Any {
        return notices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.gridview_detail_notice, parent, false)

        val notice = notices[position]

        val title = notice.getString("title") ?: "제목 없음"
        val content = notice.getString("content") ?: "내용 없음"

        val titleTextView = view.findViewById<TextView>(R.id.notice_title)
        val contentTextView = view.findViewById<TextView>(R.id.notice_content)

        titleTextView.text = title
        contentTextView.text = content

        return view
    }
}
