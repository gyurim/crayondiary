package org.androidtown.crayondiary.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.ImageView
import org.androidtown.crayondiary.data.Diary
import org.androidtown.crayondiary.canvas.CanvasActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import android.util.Log
import android.widget.Toast
import org.androidtown.crayondiary.data.AppDatabase
import org.androidtown.crayondiary.util.showToast
import org.androidtown.crayondiary.view.ViewActivity


class DrawFileUtils {
    companion object {
        private fun getFilePath(fileId: String): String {
            // 내부 저장소(기기 자체 제공) 경로 얻는 방법
            /*
            * 내부 저장소 : 지정된 파일은 해당 앱에서만 접근 가능
            * 외부 저장소 : 모든 사람이 읽기 가능 (공유 때, 적합)
            */
            return "${MainApplication.context.filesDir}/draw/$fileId"
        }

        fun setDrawFile(view: CanvasActivity, fileId: String) {
            // 기존의 파일을 수정할 때 사용! 해당 이미지 파일을 불러옴
            if (fileId.isNotEmpty()) {
                view.mBitmap = BitmapFactory.decodeFile(getFilePath(fileId))
                // decodeFile(String pathName): 휴대폰 안에 파일 형태로 저장된 이미지를 Bitmap으로 만들 때 사용
            } else {
                view.mBitmap = null
            }
        }

        fun setImage(imageView: ImageView, drawFileId: String) {
            // ViewActivity에 canvas 이미지 파일 출력
            if (drawFileId.isNotEmpty()) {
                Log.d("DrawFileUtils", "drawFileId: $drawFileId")
                imageView.setImageURI(Uri.parse(getFilePath(drawFileId)))
            } else {
                imageView.setImageResource(org.androidtown.crayondiary.R.drawable.abc_ratingbar_material)
            }
        }

        fun setScreenshot(imageView: ImageView, screenshotId: String) {
            // main recyclerview에 이미지 파일 출력
            if (screenshotId.isNotEmpty()) {
                imageView.setImageURI(Uri.parse(getFilePath(screenshotId)))
            } else {
                imageView.setImageResource(org.androidtown.crayondiary.R.drawable.abc_ratingbar_material)
            }
        }

        private fun saveImage(bitmap: Bitmap?, id: Int): String {
            bitmap?.let {
                val bytes = ByteArrayOutputStream()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, 960, true) // 원본 크기의 bitmap을 축소 (따라서, 메모리는 줄어들지 않음) 

                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val drawDirectory = File(MainApplication.context.filesDir, "draw")
                if (!drawDirectory.exists()) {
                    drawDirectory.mkdirs()
                }

                try {
                    val fileName = "${id}_${Date().time}.png"
                    val f = File(drawDirectory, fileName)
                    f.createNewFile()
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    return fileName
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
                return ""
            } ?: return ""
        }

        fun saveDrawFile(view: CanvasActivity, diary: Diary) {
            // saveDrawFile 함수 : 사용자가 그린 canvas 저장
            var bitmap = view.mBitmap
            val path = saveImage(bitmap, diary.id)
            val oldFileId = diary.drawFileId

            File(getFilePath(oldFileId)).delete()
            diary.drawFileId = path
            AppDatabase.instance.diaryDao().update(diary)

            bitmap!!.recycle()
            Log.d("DrawFileUtils", "$diary")
        }

        fun saveScreenShot(view: View, diary: Diary){
            var bitmap = getBitmapFromView(view, diary.id)
            val path = getScreenShotPath(bitmap, diary.id)
            val oldFileId = diary.screenshotId

            File(getFilePath(oldFileId)).delete()
            diary.screenshotId = path
            AppDatabase.instance.diaryDao().update(diary)

            bitmap!!.recycle() // 위치가 틀렸음. activity가 종료되는 onDestroy()에서 선언되어야함
            Log.d("DrawFileUtils", "$diary")
        }

        private fun getScreenShotPath(bitmap: Bitmap, id: Int): String {
            bitmap?.let {
                val bytes = ByteArrayOutputStream()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, 960, true)
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val screenShotDirectory = File(MainApplication.context.filesDir, "draw")

                if (!screenShotDirectory.exists())
                    screenShotDirectory.mkdirs()

                try {
                    val fileName = "${id}_${id}_${Date().time}.png"
                    Log.d("DrawFileUtils", "fileName: $fileName")

                    val f = File(screenShotDirectory, fileName)
                    f.createNewFile()

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    return fileName
                }
                catch (e1: IOException) {
                    e1.printStackTrace()
                }
                return ""
            }
        }

        private fun getBitmapFromView(view: View, id: Int): Bitmap{
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val bgDrawable : Drawable = view.background

            if(bgDrawable != null){
                bgDrawable.draw(canvas)
            }
            else{
                canvas.drawColor(Color.WHITE)
            }
            view.draw(canvas)
            return bitmap
        }


        fun toGallery(layout: View):String {
            val bitmap = capture(layout)
            val bytes = ByteArrayOutputStream() // 압축된 바이트 배열을 담을 stream

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, 960, true)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

            val sdcardStat : String  = Environment.getExternalStorageState()

            if(sdcardStat != Environment.MEDIA_MOUNTED){
                Log.d("DrawFileUtils", "sd card mount 안됨")
                return ""
            }
            else{
                val photoDirectory = File(Environment.getExternalStorageDirectory(), "/crayondiary")

                Log.d("DrawFileUtils", "external directory : ${Environment.getExternalStorageDirectory()}")
                Log.d("DrawFileUtils", "photo directory : $photoDirectory")

                photoDirectory.mkdirs()

                return try {
                    val f = File(photoDirectory, "${Date().time}.png")

                    if(!f.parentFile.exists()){
                        photoDirectory.parentFile.mkdirs()
                    }
                    if(!f.exists()){
                        f.createNewFile()
                    }

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    MediaScannerConnection.scanFile(MainApplication.context, arrayOf(f.absolutePath), null, null)
                    f.absolutePath
                } catch (e1: IOException) {
                    e1.printStackTrace()
                    Log.d("DrawFileUtils", "file 예외 발생")
                    ""
                }

            }
        }

        private fun capture(v: View): Bitmap {
            v.isDrawingCacheEnabled = true
            v.buildDrawingCache(true)
            val b = Bitmap.createBitmap(v.drawingCache)
            v.isDrawingCacheEnabled = false
            return b
        }


    }
}
