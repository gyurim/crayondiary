package org.androidtown.crayondiary.presentation.ui.main

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.androidtown.crayondiary.R
import org.androidtown.crayondiary.data.AppDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MainRecyclerViewAdapter
    private lateinit var layoutManager: GridLayoutManager
    private val viewModel: MainViewModel by viewModels()
    var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        attachButtonEvents()
        initRecyclerView()
    }

    // 저장소에서 데이터를 불러오거나, 네트워크 작업 등 이후에 뷰를 그려야할 경우 사용
    // -> 해당 프로젝트에서는 사용 X
    private fun initSplashScreen() {
        lifecycleScope.launch {
            delay(SPLASH_TIME_MILLIS)
            isReady = true
        }

        // set up on OnPreDrawListener to the root view
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener { // 뷰가 그려지기 전
                override fun onPreDraw(): Boolean {
                    // check if the initial data is ready
                    return if (isReady) {
                        // the content is ready; start drawing
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true // true 반환 시 뷰가 그려지기 시작하며 활동 시작
                    } else {
                        // the content is not ready; suspend
                        false // false 반환 시 뷰가 그려지는 것이 중단
                    }
                }
            }
        )
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
        private const val SPLASH_TIME_MILLIS = 3_000L
    }
}
