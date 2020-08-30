package com.yuedev.imagehandletest.view

import android.content.Context
import android.graphics.*
import android.text.style.TtsSpan
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by Yue on 2020/8/28.
 *
 * 测试自定义的手势检测
 */
class MyScaleView : View {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var imageBitmap: Bitmap? = null
    private val imageMatrix = Matrix()
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    private val myGestureDetector = object : MyGestureDetector(context) {


        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            imageMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
            return true
        }


        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            detector?.let {
                imageMatrix.postScale(it.scaleFactor, it.scaleFactor, it.focusX, it.focusY)
                invalidate()
                Log.d("YUEDEVTAG", "scale is done")
                return true
            } ?: return false
        }


        override fun onRotation(beginDegree: Float, prevDegree: Float, currentDegree: Float) {
            imageMatrix.postRotate(currentDegree - prevDegree, measuredWidth / 2f, measuredHeight / 2f)
            invalidate()
        }

    }



    fun setImageBitmap(bitmap: Bitmap) {
        post {
            imageBitmap = bitmap
            val scaleW = measuredWidth / bitmap.width.toFloat()
            val scaleH = measuredHeight / bitmap.height.toFloat()
            val scale = scaleW.coerceAtMost(scaleH)
            imageMatrix.setScale(scale, scale)
            imageMatrix.postTranslate(
                (measuredWidth - bitmap.width * scale) / 2f,
                (measuredHeight - bitmap.height * scale) / 2f
            )
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        imageBitmap?.let {
            canvas?.drawBitmap(it, imageMatrix, imagePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return false

        if (event.actionMasked == MotionEvent.ACTION_DOWN && !pointInImage(event.x, event.y)) return false

        return myGestureDetector.onTouchEvent(event)
    }

    //判断落点是否在图像中内部
    private fun pointInImage(x: Float, y: Float): Boolean {

        imageBitmap?.let {
            //首先进行坐标变换，将matrix的坐标映射成现实坐标，这里用图片的4个顶点进行映射
            val wFloat = it.width.toFloat()
            val hFloat = it.height.toFloat()

            val floats = floatArrayOf(0f, 0f, wFloat, 0f, 0f, hFloat, wFloat, hFloat)

            imageMatrix.mapPoints(floats)


            //利用path生产region 判断点是否在区域内部
            val path = Path()
            path.moveTo(floats[0], floats[1])
            path.lineTo(floats[2], floats[3])
            path.lineTo(floats[6], floats[7])
            path.lineTo(floats[4], floats[5])
            path.close()

            val region = Region()
            region.setPath(path, Region(0, 0, width, height))
            return region.contains(x.toInt(), y.toInt())
        } ?: return false

    }


}