package com.yuedev.imagehandletest.view

import android.content.Context
import android.graphics.*
import android.telephony.AccessNetworkConstants
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.yuedev.imagehandletest.R

/**
 * Created by Yue on 2020/8/20.
 */
class StickerView : View {

    val bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.mipmap.icons_sticker_1)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val point = PointF()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        point.x = w / 2f
        point.y = h / 2f
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bitmap, point.x - bitmap.width / 2, point.y - bitmap.height / 2, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                if ((event.x) in (point.x - bitmap.width / 2f..point.x + bitmap.width / 2f) &&
                    (event.y) in (point.y - bitmap.height / 2f..point.y + bitmap.height / 2f)
                ) {
                    point.x = event.x
                    point.y = event.y
                    invalidate()
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                point.x = event.x
                point.y = event.y
                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                false
            }
            MotionEvent.ACTION_CANCEL -> {
                false
            }
            else -> {
                false
            }

        }
    }

}