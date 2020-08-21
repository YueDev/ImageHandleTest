package com.yuedev.imagehandletest.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.permissionx.guolindev.PermissionX
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.adapter.StickerAdapter
import com.yuedev.imagehandletest.bean.Sticker
import com.yuedev.imagehandletest.getStickers
import com.yuedev.imagehandletest.loadImageWithUri
import com.yuedev.imagehandletest.savePhotoWithBitmap
import com.yuedev.imagehandletest.view.StickerView
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


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

        initRecyclerView()
        initBgView()
        initView()

    }


    private fun initRecyclerView() {

        stickerRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val adapter = StickerAdapter {
            val sticker = getStickers()[it]
            addSticker(sticker)
        }

        stickerRecyclerView.adapter = adapter

        adapter.submitList(getStickers())

    }


    private fun initBgView() {
        val imgUri = arguments?.getParcelable<Uri>("imageUri") ?: return

        //uri加载图片google推荐在io线程
        MainScope().launch {

            progressBar.visibility = View.VISIBLE
            val imageBitmap = loadImageWithUri(requireContext(), imgUri)
            bgView.setImageBitmap(imageBitmap)
            progressBar.visibility = View.GONE

        }
    }


    private fun initView() {
        saveButton.setOnClickListener {
            getPermissionAndSavePhoto()
        }
    }


    private fun addSticker(sticker: Sticker) {
        val stickerView = StickerView(requireContext())
        frameLayout.addView(stickerView)
        val bitmap = BitmapFactory.decodeResource(resources, sticker.imgResId)
        stickerView.setImageBitmap(bitmap)


        stickerView.setRectOnlyOne = {
            for (i in 1 until frameLayout.childCount) {
                val view = frameLayout[i] as StickerView
                if (view != it) view.setShowRect(false)
            }
        }


        stickerView.setCloseView = {
            frameLayout.removeView(it)
        }

    }


    //api28以下要确保存储权限，28以上分区存储不需要权限
    private fun getPermissionAndSavePhoto() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request { allGranted, _, _ ->
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

        val bg = Bitmap.createBitmap(bgView.width, bgView.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bg)


        bgView.getResultBitmap(canvas)


        for (i in 1 until frameLayout.childCount) {
            val view = frameLayout[i] as StickerView
            view.getResultBitmap(canvas)
        }

        savePhotoWithBitmap(requireContext(), bg, "newImg_${SystemClock.elapsedRealtime()}") {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            bg.recycle()
        }
    }


}