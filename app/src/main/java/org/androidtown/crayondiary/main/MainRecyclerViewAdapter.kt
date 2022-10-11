package org.androidtown.crayondiary.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_main.view.*
import org.androidtown.crayondiary.data.Diary
import org.androidtown.crayondiary.util.DrawFileUtils
import org.androidtown.crayondiary.R
import org.androidtown.crayondiary.view.ViewActivity

class MainRecyclerViewAdapter(
    private val context: Context,
    val items: MutableList<Diary> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /*onCreateViewHolder : recyclerView의 행을 표시하는데 사용되는 layout xml을 가져옴*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return MainRecyclerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false))
    }

    /*RecyclerView의 행에 보여질 ImageView와 TextView 설정*/
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindDefaultView(holder as MainRecyclerViewHolder, position)
    }

    private fun bindDefaultView(holder: MainRecyclerViewHolder, position: Int){
        val item = items[position]

        //holder.itemView.item_main_date.text 와 같은 것들을 mainRecyclerViewHolder에 변수 선언해놓는다면 매번 호출될때마다 findViewById가 사용되는 리소스를 줄일 수 있을 것임 -> 고치삼
        holder.itemView.item_main_date.text = item.date
        DrawFileUtils.setScreenshot(holder.itemView.item_main_screenshot_img, item.screenshotId)

        Log.d(TAG, "$item")

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewActivity::class.java)
            intent.putExtra("idToView", item.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    companion object {
        private const val TAG = "MainRecyclerViewAdapter"
    }

}