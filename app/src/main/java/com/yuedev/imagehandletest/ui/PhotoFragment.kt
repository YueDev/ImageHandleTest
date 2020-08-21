package com.yuedev.imagehandletest.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.get
import com.permissionx.guolindev.PermissionX
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.savePhotoWithBitmap
import com.yuedev.imagehandletest.view.StickerView
import kotlinx.android.synthetic.main.fragment_photo.*


class PhotoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        button.setOnClickListener {
//            val stickerView = StickerView(requireContext())
//            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
//            stickerView.layoutParams = params
//            workSpaceLayout.addView(stickerView)
//        }


        buttonSave.setOnClickListener {
            getPermissionAndSavePhoto()
        }

    }


    private fun getPermissionAndSavePhoto() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        saveBitmap()
                    } else {
                        Toast.makeText(requireContext(), "没有存储权限 -_-", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            saveBitmap()
        }
    }


    private fun saveBitmap() {

//        val bg = Bitmap.createBitmap(bgView.width, bgView.height, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(bg)
//
//        canvas.drawColor(Color.WHITE)
//
//        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//
//        canvas.drawBitmap(bgView.bgBitmap, 0f,0f,paint)
//
//        canvas.drawBitmap(bgView.bitmap, bgView.bitmapMatrix, paint)


//        repeat(workSpaceLayout.childCount) {
//            val stickerView = workSpaceLayout[it] as StickerView
//            val bitmap = stickerView.bitmap
//            val left = stickerView.point.x - bitmap.width / 2
//            val top = stickerView.point.y - bitmap.width / 2
//
//            canvas.drawBitmap(bitmap, left, top, paint)
//        }

//        savePhotoWithBitmap(requireContext(), bg, "newImg_${SystemClock.elapsedRealtime()}") {
//            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//        }

    }

}