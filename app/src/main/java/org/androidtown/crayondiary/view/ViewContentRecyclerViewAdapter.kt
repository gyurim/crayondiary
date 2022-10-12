package org.androidtown.crayondiary.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.item_view_content.view.*
import org.androidtown.crayondiary.R

class ViewContentRecyclerViewAdapter(
    private val context : Context,
    val items: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /*onCreateViewHolder : recyclerView의 행을 표시하는데 사용되는 layout xml을 가져옴*/
    /*LayoutInflater : xml객체를 view 객체로 만들어주기 위해 사용*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewContentRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_content, parent, false))
    }

    /*RecyclerView의 행에 보여질 ImageView와 TextView 설정*/
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as ViewContentRecyclerViewHolder, position+1)
    }

    private fun bindDefaultView(holder: ViewContentRecyclerViewHolder, position: Int) {
        if (position < items.size ) {
            holder.itemView.item_view_content_text.text = items[position]
        } else {
            holder.itemView.item_view_content_text.text = ""
        }
    }

    override fun getItemCount(): Int {
        return 70
    }
}