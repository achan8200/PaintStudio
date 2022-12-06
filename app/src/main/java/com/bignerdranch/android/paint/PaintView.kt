package com.bignerdranch.android.paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bignerdranch.android.paint.CanvasFragment.Companion.bitmap
import com.bignerdranch.android.paint.CanvasFragment.Companion.drawing
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class PaintView : View {

    private var params : ViewGroup.LayoutParams? = null
    private var mX : Float ?= null
    private var mY : Float ?= null
    private var touchTolerance : Float = 4f

    companion object {
        lateinit var mBitmap : Bitmap
        var mCanvas : Canvas = Canvas()
        var drawings : ArrayList<Drawing> = ArrayList()
        var undoneDrawings : ArrayList<Drawing> = ArrayList()
        var currentBrush = Color.BLACK
        var currentWidth = 10f
    }

    constructor(context: Context) : this(context, null) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initPaintbrush()
        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initPaintbrush() {
        drawing.paintBrush.color = currentBrush
        drawing.paintBrush.strokeWidth = currentWidth
        drawing.paintBrush.style = Paint.Style.STROKE
        drawing.paintBrush.strokeJoin = Paint.Join.ROUND
        drawing.paintBrush.isAntiAlias = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = try {
            if (bitmap == "") {
                Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            } else {
                stringToBitmap(bitmap)
            }
        } catch (e: Exception) {
            Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        }
        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
            else -> {}
        }
        invalidate()
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        drawing = Drawing()
        initPaintbrush()
        drawings.add(drawing)
        drawing.path.reset()
        drawing.path.moveTo(x,y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dX : Float = abs(x - mX!!)
        val dY : Float = abs(y - mY!!)
        if (dX >= touchTolerance || dY >= touchTolerance) {
            drawing.path.quadTo(mX!!, mY!!, (x + mX!!) / 2, (y + mY!!) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        drawing.path.lineTo(mX!!, mY!!)
    }

    override fun onDraw(canvas: Canvas) {
        mCanvas.setBitmap(mBitmap)
        for (i in drawings.indices) {
            mCanvas.drawPath(drawings[i].path, drawings[i].paintBrush)
            canvas.drawBitmap(mBitmap, 0f, 0f, drawing.paintBrush)
            invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stringToBitmap(encodedString: String): Bitmap {
        val encodeByte: ByteArray = Base64.getDecoder().decode(encodedString)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }

    fun setUndo() {
        if (drawings.isNotEmpty()) {
            undoneDrawings.add(drawings.removeAt(drawings.size - 1))
            invalidate()
        }
    }

    fun setRedo() {
        if (undoneDrawings.isNotEmpty()) {
            drawings.add(undoneDrawings.removeAt(undoneDrawings.size - 1))
            invalidate()
        }
    }
}