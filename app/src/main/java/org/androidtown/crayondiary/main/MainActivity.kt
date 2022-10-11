package org.androidtown.crayondiary.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.androidtown.crayondiary.canvas.ModifyActivity
import org.androidtown.crayondiary.R
import org.androidtown.crayondiary.data.AppDatabase


class MainActivity : Activity() {
    private lateinit var adapter: MainRecyclerViewAdapter
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachButtonEvents()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadDiaries()
    }

    private fun loadDiaries() {
        adapter.items.clear()
        adapter.items.addAll(AppDatabase.instance.diaryDao().getAll())
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        adapter = MainRecyclerViewAdapter(this)
        main_recycler.adapter = adapter
        layoutManager = GridLayoutManager(this, 2)
        main_recycler.layoutManager = layoutManager
    }

    private fun attachButtonEvents() {
        main_write_button.setOnClickListener {
            startActivity(Intent(this, ModifyActivity::class.java))
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}
