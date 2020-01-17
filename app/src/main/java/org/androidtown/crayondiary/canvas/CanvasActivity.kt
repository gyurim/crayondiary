package org.androidtown.crayondiary.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CanvasActivity : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private var mPaint = Paint()
    private var erasePaint = Paint()
    private var mPath = Path()

    var mBitmap: Bitmap? = null
        set(value) {
            value?.let {
                field = Bitmap.createScaledBitmap(it, width, height, true) //true: pixel형태 조정해 이미지 선명하게 보이도록
            } ?: run {
                field = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) //한 pixel 4 bytes씩
            }
            mCanvas = Canvas(field)
            invalidate()
        }

    private var mCanvas: Canvas? = null
    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var eraseMode = false

    init {
        mPaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
            isAntiAlias = true
        }

        erasePaint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
            isAntiAlias = true
            erasePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    override fun onDraw(canvas: Canvas) {
        mBitmap?.let {
            canvas.drawBitmap(mBitmap, Rect(0, 0, it.width, it.height), Rect(0, 0, it.width, it.height), null)
        }
    }

    fun changeColor(color: Int) {
        if(!eraseMode){
            mPaint.color = color
        }
    }

    fun changeWidth(width: Float) {
        if(eraseMode){
            erasePaint.strokeWidth = width
        }else{
            mPaint.strokeWidth = width
        }
    }

    fun changeType(type : Int){
        when (type) {
            0 -> /*want: crayon*/
                eraseMode = false
            else -> /*eraser*/
                eraseMode = true
        }
    }

    /* PorterDuff.Mode는 pixel 정보를 조합하는 거라고 생각하면 됨
    * .CLEAR 즉, CLEAR는 연산자 중 하나이다. 기능 : 겹치면 아무 색도 표현되지 않게 함
    * mPaint와 erasePaint가 겹치면 지워지게 함
    * */

    fun clear() {
        mCanvas?.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        if (eraseMode) {
            mCanvas?.drawPath(mPath, erasePaint)
        } else {
            mCanvas?.drawPath(mPath, mPaint)
        }
        mCurX = x
        mCurY = y
    }

    private fun actionUp() {
        mPath.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }
        invalidate()
        return true
    }

    companion object {
        private const val TAG = "CanvasActivity"
    }

}