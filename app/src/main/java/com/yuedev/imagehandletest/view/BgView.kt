package com.yuedev.imagehandletest.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.blurBitmap
import kotlin.math.sqrt

/**
 * Created by Yue on 2020/8/20.
 */


class BgView : View {

    private val modeNone = 0
    private val modeDrag = 1
    private val modeZoom = 2


    private var mode = modeNone


    //第一个手指按下的坐标
    private val startPoint = PointF()

    //第一个手指上一次点的记录
    private val lastPoint = PointF()

    //双指触摸，中间的点的坐标
    private val middlePoint = PointF()

    //双指触摸，双指间的初始距离,因为有除法，初始1
    private var startDis = 1f
    //双指上一次的距离
    private var lastDis = 1f

    //双指缩放的角度


    private val imageBitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.mipmap.image)

    }

    private val bgBitmap by lazy {
        blurBitmap(context, imageBitmap, width, height, 25f)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val imageMatrix = Matrix()


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


        val scale = (w / imageBitmap.width.toFloat()).coerceAtMost(h / imageBitmap.height.toFloat())
        imageMatrix.setScale(scale, scale)
        //postTranslate在setScale之后用，就能用现实坐标移动对齐matrix之后的目标
        imageMatrix.postTranslate(
            (w - imageBitmap.width * scale) / 2,
            (h - imageBitmap.height * scale) / 2
        )


    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return false

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //首先判断手指是否在图像内部
                if (pointInImage(x, y)) {
                    mode = modeDrag
                    startPoint.x = x
                    startPoint.y = y
                    lastPoint.x = x
                    lastPoint.y = y
                } else return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {

                //多点 这里只处理2点的情况
                val newX = event.getX(1)
                val newY = event.getY(1)
                val newDis  = calDis(x, y, newX, newY)
                //如果两点的距离大于10f，则开始处理多点情况
                if (pointInImage(newX, newY) && (newDis) > 10f) {
                    mode = modeZoom
                    startDis = newDis
                    lastDis = newDis

                    val pair = calMiddle(x, y, newX, newY)
                    middlePoint.x = pair.first
                    middlePoint.y = pair.second
                } else {
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> {

                when {
                    //拖拽
                    mode == modeDrag -> {
                        val dx = x - lastPoint.x
                        val dy = y - lastPoint.y
                        imageMatrix.postTranslate(dx, dy)
                        lastPoint.x = x
                        lastPoint.y = y
                    }

                    //处理双指缩放
                    modeZoom == modeZoom && event.pointerCount == 2 -> {

                        val newDis = calDis(x, y, event.getX(1), event.getY(1))
                        val scale = newDis / lastDis
                        //注意缩放中心，用之前计算的middlePoint
                        imageMatrix.postScale(scale, scale, middlePoint.x, middlePoint.y)
                        lastDis = newDis
                    }

                    else -> {
                        return false
                    }
                }


            }

            MotionEvent.ACTION_UP -> {

                startDis = 1f
                lastDis = 1f

                mode = modeNone
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = modeNone
            }

            MotionEvent.ACTION_CANCEL -> {
                mode = modeNone
            }
            else -> {
                return false
            }
        }

        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(bgBitmap, 0f, 0f, paint)
        canvas?.drawBitmap(imageBitmap, imageMatrix, paint)
    }

    //计算两个坐标之间的距离
    private fun calDis(x: Float, y: Float, newX: Float, newY: Float): Float {
        val dx = newX - x
        val dy = newY - y
        return sqrt(dx * dx + dy * dy)
    }
    
    //两个坐标的中间点
    private fun calMiddle(x1: Float, y1: Float, x2: Float, y2: Float) = Pair((x1 + x2) / 2f, (y1 + y2) /2f)

    //判断落点是否在图像中内部
    private fun pointInImage(x: Float, y: Float): Boolean {

        //首先进行坐标变换，将matrix的坐标映射成现实坐标，这里用图片的4个顶点进行映射
        val wFloat = imageBitmap.width.toFloat()
        val hFloat = imageBitmap.height.toFloat()

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

    }

}