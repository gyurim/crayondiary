package org.androidtown.crayondiary.canvas

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_modify.*
import org.androidtown.crayondiary.R
import org.androidtown.crayondiary.data.AppDatabase
import org.androidtown.crayondiary.data.Diary
import org.androidtown.crayondiary.data.formatYYYYMMDD
import org.androidtown.crayondiary.util.DrawFileUtils
import org.androidtown.crayondiary.util.showToast
import org.androidtown.crayondiary.view.ViewActivity
import petrov.kristiyan.colorpicker.ColorPicker
import java.util.*
import kotlin.collections.ArrayList

class ModifyActivity : AppCompatActivity(), View.OnClickListener {
    var diary: Diary? = null

    private var weighttemp : Int = 0
    private var weathertemp : String = ""
    private var weatherId : String = ""
    private var typetemp = 0

    var curWidth = 10f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify)
        attachButtonEvents()
        initButtonListeners()

        //From ViewActivity To ModifyActivity
         if(intent.hasExtra("idToModify")){
             loadDiary()
         }
         //From MainActivity To ModifyActivity
        else {
            val vto = canvas.viewTreeObserver

            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    canvas.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    canvas.mBitmap = null
                }
            })
        }
    }

    private fun loadDiary() {
        val intentDiaryId = intent.getIntExtra("idToModify", 0)
        diary = AppDatabase.instance.diaryDao().get(intentDiaryId)
        //diary가 null이 아니면 불러옴
        diary?.let {
            when(it.weather){
                "sunny" -> { sunny.isSelected = true }

                "cloudy" -> { cloudy.isSelected = true }

                "rainy" -> { rainy.isSelected = true }

                "snowy" -> { snowy.isSelected = true }

                "rainbow" -> { rainbow.isSelected = true }
            }
            weathertemp = it.weather
            modify_content_edit.setText(it.content)


            val vto = canvas.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    canvas.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    DrawFileUtils.setDrawFile(canvas, it.drawFileId)
                }
            })
        }
    }

    private fun initButtonListeners(){
        sunny.setOnClickListener(this)
        rainbow.setOnClickListener(this)
        rainy.setOnClickListener(this)
        cloudy.setOnClickListener(this)
        snowy.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.sunny -> weatherButtonChange("sunny")
            R.id.rainbow -> weatherButtonChange("rainbow")
            R.id.rainy -> weatherButtonChange("rainy")
            R.id.cloudy->weatherButtonChange("cloudy")
            R.id.snowy -> weatherButtonChange("snowy")
        }

    }

    private fun attachButtonEvents() {
        write_back_button.setOnClickListener {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder
                    .setMessage("그림일기 작성을 취소하시겠습니까?")
                    .setPositiveButton("OK"){_,_ ->
                        finish() }
                    .setNegativeButton("NO"){_,_ -> }
                    .show()
        }
        write_done_button.setOnClickListener {
            saveDiary()
        }

        brush_type_button.setOnClickListener {
            /* crayon, eraser and so on */
            //canvas.eraseMode = !canvas.eraseMode
            type() }

        draw_back_button.setOnClickListener {
            canvas.clear() }

        brush_color_button.setOnClickListener {
            openColorPicker() }

        brush_weight_button.setOnClickListener {
            openWeightPicker() }
    }

    private fun weatherButtonChange(weatherId : String){
        val pkg = packageName
        val mId = resources.getIdentifier(weatherId, "id", pkg)
        val mWeatherImageButton : RadioButton = findViewById(mId)

       if(!mWeatherImageButton.isSelected){
            mWeatherImageButton.isSelected = true
           weathertemp = weatherId
        }
        else{
            mWeatherImageButton.isSelected = false
            weathertemp = ""
        }
    }

    private fun openWeightPicker() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select the brush size")

        val weight = arrayOf("5", "10", "15", "20")
        var checkedItem = weighttemp
        
       builder.setSingleChoiceItems(weight, checkedItem
       ) { _, which -> checkedItem = which }

        builder.setPositiveButton(
            "OK"
        ) { dialog, _ ->
            canvas.changeWidth(weight[checkedItem].toFloat())
            weighttemp = checkedItem
            curWidth = weight[checkedItem].toFloat()

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun openColorPicker() {
        val colorPicker = ColorPicker(this)
        val colors = ArrayList<String>()

        colors.add("#FFC19E")
        colors.add("#FFE08C")
        colors.add("#FAED7D")
        colors.add("#B7F0B1")
        colors.add("#B2CCFF")

        colors.add("#D1B2FF")
        colors.add("#FFB2F5")
        colors.add("#A6A6A6")//GRAY
        colors.add("#FFFFFF")//WHITE
        colors.add("#000000")//BLACK


        colors.add("#FF0000")
        colors.add("#FF7F00")
        colors.add("#FFBB00")
        colors.add("#FFE400")
        colors.add("#8CFF00")

        colors.add("#1FDA11")
        colors.add("#00D8FF")
        colors.add("#4174D9")
        colors.add("#0900FF")
        colors.add("#7E41D9")


        colorPicker.setColors(colors)
        colorPicker.show()
        colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
            override fun onChooseColor(position: Int, color: Int) {
                canvas.changeColor(color)
            }
            override fun onCancel() {}
        })
    }

    private fun type(){
        val selectType = typetemp

        when(selectType){
            0 -> { //현재: crayon 원하는 기능: eraser
                canvas.changeType(1)
                canvas.changeWidth(curWidth)
                brush_type_button.isSelected = true
                typetemp = 1
            }

            1 -> { //eraser
                canvas.changeType(0)
                canvas.changeWidth(curWidth)
                brush_type_button.isSelected = false
                typetemp = 0
            }
            else -> {
                canvas.changeType(0)
                canvas.changeWidth(curWidth)
                brush_type_button.isSelected = false
                typetemp = 0
            }
        }
    }


    private fun saveDiary() {
        weatherId = weathertemp

        if (weatherId.isNotEmpty() && modify_content_edit.text.isNotEmpty()) {
            //addMemoAPI 호출
            val intentDiaryId = intent.getIntExtra("idToModify", 0)

            Log.d(TAG, "intentDiaryId: $intentDiaryId")
            val diary = Diary(intentDiaryId, modify_content_edit.text.toString(), "", Date().formatYYYYMMDD(), weatherId, "")
            val diaryId = AppDatabase.instance.diaryDao().insert(diary)

            diary.id = diaryId.toInt()
            DrawFileUtils.saveDrawFile(canvas, diary)
            AppDatabase.instance.diaryDao().update(diary)
            Log.d(TAG, "update 후: $diary")

            val intent = Intent(this, ViewActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("diaryIdToView", diary.id)
            intent.putExtra("idToView", diary.id)
            startActivity(intent)
            finish()
        }
        else if(weatherId.isEmpty()){
            showToast { "날씨를 선택해주세요 " }
        }
        else if (modify_content_edit.text.isEmpty()){
            showToast { "일기를 작성해주세요 " }
        }
        else {
            showToast{"빈 값이 있습니다."}
        }
    }

    companion object {
        private const val TAG = "ModifyActivity"
    }
}

