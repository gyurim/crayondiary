package org.androidtown.crayondiary.view

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_view.*
import org.androidtown.crayondiary.data.Diary
import org.androidtown.crayondiary.util.DrawFileUtils
import org.androidtown.crayondiary.canvas.ModifyActivity
import org.androidtown.crayondiary.data.AppDatabase
import org.androidtown.crayondiary.util.showToast
import java.io.File

//메모의 정보를 보여주는 뷰
class ViewActivity : Activity() {
    var diary: Diary? = null

    private lateinit var adapter: ViewContentRecyclerViewAdapter
    private lateinit var layoutManager: GridLayoutManager

    private val REQUEST_CODE_MULTI = 1001
    private val REQUEST_CODE_READ = 1002
    private val REQUEST_CODE_WRITE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(org.androidtown.crayondiary.R.layout.activity_view)

        initRecyclerView()
        loadView()
        attachButtonEvents()

        if(intent.getIntExtra("diaryIdToView", 0) != 0){
            loadScreenShot()
        }
    }

    private fun initRecyclerView() {
        adapter = ViewContentRecyclerViewAdapter(this)
        view_content_text.adapter = adapter
        layoutManager = GridLayoutManager(this, 10)
        view_content_text.layoutManager = layoutManager
    }

    private fun loadView() {
        val mDiaryId = intent.getIntExtra("idToView", -1)

        Log.d(TAG, "idToView: $mDiaryId")
        diary = AppDatabase.instance.diaryDao().get(mDiaryId)
        Log.d(TAG, "$diary")

        diary?.let {
            DrawFileUtils.setImage(view_canvas_image, it.drawFileId)

            view_date_text.text = it.date
            //view_title_text.text = it.title

            if (it.weather == "sunny") {
                view_weather_image.setImageResource(org.androidtown.crayondiary.R.drawable.sun_click)
            }
            if (it.weather == "rainy") {
                view_weather_image.setImageResource(org.androidtown.crayondiary.R.drawable.rain_click)
            }
            if (it.weather == "snowy") {
                view_weather_image.setImageResource(org.androidtown.crayondiary.R.drawable.snowflake_click)
            }
            if (it.weather == "cloudy") {
                view_weather_image.setImageResource(org.androidtown.crayondiary.R.drawable.clouds_click)
            }
            if (it.weather == "rainbow") {
                view_weather_image.setImageResource(org.androidtown.crayondiary.R.drawable.rainbow_click)
            }
            val content = it.content.split("")

            adapter.items.addAll(content)
            adapter.notifyDataSetChanged()


            Log.d(TAG, "drawFileId: ${it.drawFileId}")
            Log.d(TAG, "screenshotId: ${it.screenshotId}")

        } ?: run {
            showToast { "메모가 없습니다." }
            finish()
        }
    }


    private fun attachButtonEvents() {
        view_back_button.setOnClickListener {
            finish()
        }

        view_modify_button.setOnClickListener {
            val modifyIntent = Intent(this, ModifyActivity::class.java)

            modifyIntent.putExtra("idToModify", diary!!.id)
            startActivity(modifyIntent)
            finish()
        }

        view_delete_button.setOnClickListener{
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder
                .setMessage("다이어리를 삭제하시겠습니까?")
                .setPositiveButton("OK"){_,_ ->
                    deleteDiary()
                }
                .setNegativeButton("NO"){_,_ -> }
                .show()
        }

        view_save_button.setOnClickListener {
            val isGrantStorage = grantExternalStoragePermission()
            if(isGrantStorage){
                shareDiary()
            }
            else{
                Log.d(TAG, "permission is rejected")
            }
        }
    }

    private fun grantExternalStoragePermission() : Boolean{
        if(Build.VERSION.SDK_INT >= 23){
            if(ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
              if(ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)){
                  ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), REQUEST_CODE_MULTI)
              }
                else if(ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)){
                  ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_CODE_READ)
              }
                else if(ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)){
                  ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE)
              }
                else{
                  ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), REQUEST_CODE_MULTI)
              }
            }
            else{
                Log.d(TAG,"Permission is granted")
                return true
            }
        }
        else{
            Log.d(TAG, "External Storage Permission is Grant ")
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_MULTI){
            val readPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
            val writePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED

            if(readPermission && writePermission){
                shareDiary()
            }
            else{
                /*
                if(!(readPermission && writePermission)){
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), REQUEST_CODE_MULTI)
                }
                else if(!readPermission){
                    ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_CODE_READ)
                }
                else if(!writePermission){
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE)
                }
                */
                showToast { "권한을 설정해야 공유하기가 가능해집니다" }
            }
        }
        else if(requestCode == REQUEST_CODE_WRITE){
            val writePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED

            if(writePermission){
                shareDiary()
            }
            else{
                //showToast { "권한을 설정해야 공유하기가 가능해집니다" }
                //ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE)
                Log.d(TAG, "Required Write permission")
            }
        }
        else if(requestCode == REQUEST_CODE_READ){
            val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED

            if(readPermission){
                shareDiary()
            }
            else{
                //showToast { "권한을 설정해야 공유하기가 가능해집니다"}
                //ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_CODE_READ)
                Log.d(TAG, "Required Read permission")
            }
        }
    }

    private fun shareDiary(){
        val path = DrawFileUtils.toGallery(wrap_layout)
        val imgUri : Uri
        val file = File(path)

        //File 객체의 uri 얻음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            //API 24 이상
            imgUri = FileProvider.getUriForFile(this,"org.androidtown.crayondiary.fileprovider", file)
            Log.d(TAG, "24 이상 imgUri: $imgUri")
        }
        else{
            // API 24 미만
            imgUri = Uri.fromFile(file)
            Log.d(TAG, "24 미만 imgUri: $imgUri")
        }

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder
            .setTitle("다이어리가 저장되었습니다.")
            .setMessage("다이어리를 공유하시겠습니까?")
            .setPositiveButton("OK") { _, _ ->
                try{
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "image/png"
                    share.putExtra(Intent.EXTRA_STREAM, imgUri)
                    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(Intent.createChooser(share, "Share Image"))
                }
                catch (e:Exception){
                    showToast { "sending fail" }
                }
            }
            .setNegativeButton("NO", null)
            .show()
    }

    private fun deleteDiary() {
        if (diary!!.id == -1) {
            Toast.makeText(this, "메모 ID 값을 잘 전달받지 못했습니다", Toast.LENGTH_SHORT).show()
            return
        }
        AppDatabase.instance.diaryDao().delete(diary!!.id)
        finish()
    }

    private fun loadScreenShot(){
        val vto = view_wrap_layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout(){
                view_wrap_layout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Log.d(TAG, "width: ${view_wrap_layout.width}")
                Log.d(TAG, "height: ${view_wrap_layout.height}")

                DrawFileUtils.saveScreenShot(view_wrap_layout, diary!!)
            }
        })
    }

    companion object {
        private const val TAG = "ViewActivity"
    }
}