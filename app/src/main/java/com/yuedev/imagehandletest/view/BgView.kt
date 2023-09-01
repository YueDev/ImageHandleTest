package com.yuedev.imagehandletest.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.yuedev.imagehandletest.blurBitmap
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Created by Yue on 2020/8/20.
 */


class BgView : View {

    private var imageBitmap: Bitmap? = null

    private var bgBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    private val imageMatrix = Matrix()
    private val bgMatrix = Matrix()


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val gestureDetector = object : MyGestureDetector(context) {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            imageMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
            return true
        }


        override fun onScale(detector: ScaleGestureDetector): Boolean {
            imageMatrix.postScale(
                detector.scaleFactor,
                detector.scaleFactor,
                detector.focusX,
                detector.focusY
            )
            invalidate()
            return true
        }

        override fun onRotation(
            beginDegree: Float,
            prevDegree: Float,
            currentDegree: Float,
            focusX: Float,
            focusY: Float
        ) {
            imageMatrix.postRotate(currentDegree - prevDegree, focusX, focusY)
            invalidate()
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (event.action == MotionEvent.ACTION_DOWN && !pointInImage(event.x, event.y)) {
            return false
        }
        return gestureDetector.onTouchEvent(event)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgBitmap?.let {
            canvas.drawBitmap(it, bgMatrix, paint)
        }
//
        imageBitmap?.let {
            canvas.drawBitmap(it, imageMatrix, paint)
        }

    }


    fun setImageBitmap(bitmap: Bitmap) {
        post {

            imageBitmap = bitmap
            val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            val dstRect = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat());
            imageMatrix.reset()
            imageMatrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER)


            val blurBitmap = blurBitmap(context, bitmap, bitmap.width, bitmap.height, 25f)
            this.bgBitmap = blurBitmap
            bgMatrix.set(calculateMatrixFill(blurBitmap, measuredWidth, measuredHeight))
            invalidate()

        }
    }


    // 计算bitmap填充到viewWidth viewHeight的matrix
    private fun calculateMatrixFill(bitmap: Bitmap, viewWidth: Int, viewHeight: Int): Matrix {
        val matrix = Matrix()
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // 计算缩放比例
        val scaleX = viewWidth.toFloat() / bitmapWidth.toFloat()
        val scaleY = viewHeight.toFloat() / bitmapHeight.toFloat()
        val scale = maxOf(scaleX, scaleY)
        matrix.postScale(scale, scale)

        // 计算位移
        val scaledBitmapWidth = bitmapWidth * scale
        val scaledBitmapHeight = bitmapHeight * scale
        val dx = (viewWidth - scaledBitmapWidth) / 2f
        val dy = (viewHeight - scaledBitmapHeight) / 2f
        matrix.postTranslate(dx, dy)

        return matrix
    }


    //判断落点是否在图像中内部
    private fun pointInImage(x: Float, y: Float): Boolean {
        val bitmap = imageBitmap ?: return false
        val matrix = Matrix()
        val b = imageMatrix.invert(matrix)
        if (!b) return false
        val array = floatArrayOf(x, y)
        matrix.mapPoints(array)
        return array[0] > 0f && array[1] > 0f && array[0] < bitmap.width && array[1] < bitmap.height
    }

    fun getResultBitmap(canvas: Canvas) {
        bgBitmap?.let { canvas.drawBitmap(it, bgMatrix, paint) }
        imageBitmap?.let { canvas.drawBitmap(it, imageMatrix, paint) }
    }

}