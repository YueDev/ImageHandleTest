package com.yuedev.imagehandletest

/**
 * Created by Yue on 2020/8/20.
 */

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import com.yuedev.imagehandletest.bean.Sticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Created by Yue on 2020/8/18.
 */




fun getStickers() = listOf(
    Sticker("Google+", R.mipmap.icons_sticker_1),
    Sticker("Pinterest", R.mipmap.icons_sticker_2),
    Sticker("Instagram", R.mipmap.icons_sticker_3),
    Sticker("Facebook", R.mipmap.icons_sticker_4),
    Sticker("Calendar", R.mipmap.icons_sticker_5),
)



//io线程读取图片的uri， suspend挂起函数，协程中用

suspend fun loadImageWithUri(context: Context, imageUri: Uri) =

    withContext(Dispatchers.IO) {
        val fileDescriptor =
            context.contentResolver.openFileDescriptor(imageUri, "r") ?: throw NullPointerException("can not openFileDescriptor, check uri")
        Log.d("YUEDEVTAG", "loadImageWithUri: as;lodjaspodjmaspodkajmspo[dasd")
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.fileDescriptor)
        fileDescriptor.close()
        bitmap
    }


//分区存储 利用MediaStore存储图片
//API28之前的版本 要先确保写存储权限
//context ：   用于获取contentResolver
//bitmap:      图片源
//fileName：   图片名称，会查重
//showResult： 存储结果的回调，在主线程


fun savePhotoWithBitmap(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
    showResult: (result: String) -> Unit
) {

    MainScope().launch {

        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver = context.contentResolver

        //先进行图片查重
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        //如果图片很多是否会很慢？切到IO线程去查
        val hasPhoto = withContext(Dispatchers.IO) {
            resolver.query(externalUri, projection, selection, selectionArgs, null)?.use {
                it.count > 0
            }
        }


        if (hasPhoto == true) {
            showResult("图片已经存在！")
            return@launch
        }

        //API29以上，设置IS_PENDING状态为1，这样存储结束前，其他应用就不会处理这张图片
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        //这里insertUri是nullable的，所以需要判空
        val insertUri = resolver.insert(externalUri, values) ?: let {
            showResult("出错了，请稍后再试")
            return@launch
        }

        //存储也放在IO线程，防止大图片耗时
        val result = withContext(Dispatchers.IO) {
            //use用在closeable对象，可以自动关闭它们
            resolver.openOutputStream(insertUri)!!.use {

                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)) {

                    //api 29以上  IS_PENDING置0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(insertUri, values, null, null)
                    }
                    "保存成功，快去相册看看吧^-^"

                } else {
                    "出错了，请稍后再试"
                }
            }
        }

        showResult(result)

    }

}



//这个模糊方法已经过时，需要修改，
//之前试过paint的BlurMaskFilter，但是只能模糊图片边缘，并且在8.0的sony SO-02上无效（硬件加速/离屏渲染都无效）。


//模糊图片的具体方法

// context 上下文对象
// bitmap   需要模糊的图片
// outWidth  blurBitmap的宽
// outHeight  blurBitmap的高
// blurRadius 模糊的半径
// 模糊处理后的图片

fun blurBitmap(
    context: Context?,
    bitmap: Bitmap,
    outWidth: Int,
    outHeight: Int,
    blurRadius: Float
): Bitmap {

    val inputBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, false)

    // 创建一张渲染后的输出图片
    val outputBitmap = Bitmap.createBitmap(inputBitmap)


    // 创建RenderScript内核对象
    val rs = RenderScript.create(context)
    // 创建一个模糊效果的RenderScript的工具对象
    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

    // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
    // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
    val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
    val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
    // 设置渲染的模糊程度, 25f是最大模糊度
    blurScript.setRadius(blurRadius)
    // 设置blurScript对象的输入内存
    blurScript.setInput(tmpIn)
    // 将输出数据保存到输出内存中
    blurScript.forEach(tmpOut)
    // 将数据填充到Allocation中
    tmpOut.copyTo(outputBitmap)

    val wScale = outWidth / outputBitmap.width.toFloat()
    val hScale = outHeight / outputBitmap.height.toFloat()

    val matrix = Matrix()
    matrix.setScale(wScale, hScale)

    return Bitmap.createBitmap(
        outputBitmap,
        0,
        0,
        outputBitmap.width,
        outputBitmap.height,
        matrix,
        false
    )
}