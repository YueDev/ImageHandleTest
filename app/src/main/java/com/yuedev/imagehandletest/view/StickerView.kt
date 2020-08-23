package com.yuedev.imagehandletest.view

import android.content.Context
import android.graphics.*
import android.telephony.AccessNetworkConstants
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.blurBitmap
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Created by Yue on 2020/8/20.
 *
 *
 * 可以和bgView封装一下。后续会做
 *
 */
class StickerView : View {


    private val closeBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.icon_close)
    }

    private val closeMatrix = Matrix()


    private val modeNone = 0
    private val modeDrag = 1
    private val modeZoom = 2

    //边框模式
    private var isRect = false

    private var mode = modeNone


    //第一个手指按下的坐标
    private val startPoint = PointF()

    //第一个手指上一次点的记录
    private val lastPoint = PointF()


    //双指触摸，双指间的初始距离,因为有除法，初始1
    private var startDis = 1f

    //双指上一次的距离
    private var lastDis = 1f

    //双指触摸，中间的点的坐标,用来确定缩放的中心
    private val middlePoint = PointF()


    //上一次的旋转角度
    private var lastRotation = 0f


    private var imageBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imageMatrix = Matrix()

    //边框 左上顶点 右上顶点 左下 右下
    private val rectPoints = FloatArray(8)
    private val mapRectPoints = FloatArray(8)
    private val rectPath = Path()
    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AD5BA3")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }


    var setRectOnlyOne: ((stickerView: StickerView) -> Unit)? = null
    var setCloseView: ((stickerView: StickerView) -> Unit)? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return false

        imageBitmap ?: return false


        val x = event.x
        val y = event.y

        //判断close点击
        if (isRect && event.actionMasked == MotionEvent.ACTION_DOWN) {
            val dis = calDis(x, y, mapRectPoints[0], mapRectPoints[1])
            if (dis <= closeBitmap.width / 2f) {
                setCloseView?.invoke(this)
                return true
            }

        }


        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //首先判断手指是否在图像内部
                if (pointInImage(x, y)) {
                    mode = modeDrag
                    startPoint.x = x
                    startPoint.y = y
                    lastPoint.x = x
                    lastPoint.y = y

                    //处理外边框矩形,注意 要设置自己是其唯一有矩形框的
                    isRect = true
                    setRectOnlyOne?.invoke(this)
                } else {
                    isRect = false
                    invalidate()
                    return false
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                //多点 这里只处理2点的情况
                val newX = event.getX(1)
                val newY = event.getY(1)
                val newDis = calDis(x, y, newX, newY)
                //如果两点的距离大于10f，则开始处理多点情况
                if (pointInImage(newX, newY) && (newDis) > 10f) {
                    mode = modeZoom
                    startDis = newDis
                    lastDis = newDis

                    val pair = calMiddle(x, y, newX, newY)
                    middlePoint.set(pair.first, pair.second)
                    lastRotation = calRotation(x, y, newX, newY)

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


                        //计算出与上次旋转的角度差，然后post即可
                        val nowRotation = calRotation(x, y, event.getX(1), event.getY(1))
                        val postRotation = nowRotation - lastRotation

                        imageMatrix.postRotate(
                            postRotation,
                            measuredWidth / 2f,
                            measuredHeight / 2f
                        )

                        //注意旋转完了获取下当前角度，赋值给lastRotation，下次旋转用
                        lastRotation = calRotation(x, y, event.getX(1), event.getY(1))

                    }

                    else -> {
                        return false
                    }
                }


            }

            MotionEvent.ACTION_UP -> {
                mode = modeNone
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = when (event.actionIndex) {
                    0 -> {
                        startPoint.set(event.getX(1), event.getY(1))
                        lastPoint.set(startPoint)
                        modeDrag
                    }
                    1 -> {
                        startPoint.set(event.getX(0), event.getY(0))
                        lastPoint.set(startPoint)
                        modeDrag
                    }
                    else -> {
                        return false
                    }
                }
            }


            MotionEvent.ACTION_CANCEL -> {
                mode = modeNone
            }

            else -> {
                return false
            }

        }


        if (isRect) imageMatrix.mapPoints(mapRectPoints, rectPoints)


        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        imageBitmap?.let {
            canvas?.drawBitmap(it, imageMatrix, paint)

            //画矩形和角标
            if (isRect) {
                rectPath.moveTo(mapRectPoints[0], mapRectPoints[1])
                rectPath.lineTo(mapRectPoints[2], mapRectPoints[3])
                rectPath.lineTo(mapRectPoints[6], mapRectPoints[7])
                rectPath.lineTo(mapRectPoints[4], mapRectPoints[5])
                rectPath.close()
                canvas?.drawPath(rectPath, rectPaint)
                rectPath.reset()

                closeMatrix.postTranslate(-closeBitmap.width / 2f, -closeBitmap.height / 2f)
                closeMatrix.postConcat(imageMatrix)
                canvas?.drawBitmap(closeBitmap, closeMatrix, paint)
                closeMatrix.reset()


            }

        }


    }


    fun release() {
        imageBitmap?.recycle()
        imageBitmap = null
    }


    fun setImageBitmap(imageBitmap: Bitmap) {



        //这里view刚add完，所以要等等
        post {

            this.imageBitmap = imageBitmap
            //边框坐标确定，比图像大一些，space是间距

            val w = imageBitmap.width
            val h = imageBitmap.height
            val space = 0f
            //lt left_top
            rectPoints[0] = 0 - space
            rectPoints[1] = 0 - space
            //lr
            rectPoints[2] = w + space
            rectPoints[3] = 0 - space
            //lb
            rectPoints[4] = 0 - space
            rectPoints[5] = h + space
            //rb
            rectPoints[6] = w + space
            rectPoints[7] = h + space


            imageMatrix.postTranslate(
                abs(measuredWidth - w) / 2f,
                abs(measuredHeight - h) / 2f
            )

            invalidate()
        }

    }


    //计算两个坐标之间的距离
    private fun calDis(x: Float, y: Float, newX: Float, newY: Float): Float {
        val dx = newX - x
        val dy = newY - y
        return sqrt(dx * dx + dy * dy)
    }

    //两个坐标的中间点
    private fun calMiddle(x1: Float, y1: Float, x2: Float, y2: Float) =
        Pair((x1 + x2) / 2f, (y1 + y2) / 2f)

    //计算两个点的角度
    private fun calRotation(x: Float, y: Float, newX: Float, newY: Float): Float {
        val dx = newX - x
        val dy = newY - y
        //利用斜切转化为极坐标，相当于把两个点的线迁移到0，0，然后转化为极坐标，得到弧度
        val angle = atan2(dy, dx)
        return Math.toDegrees(angle.toDouble()).toFloat()
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

    fun setShowRect(isShowRect: Boolean) {
        isRect = isShowRect
        invalidate()
    }


    fun getResultBitmap(canvas: Canvas) {
        imageBitmap?.let { canvas.drawBitmap(it, imageMatrix, paint) }
    }

}