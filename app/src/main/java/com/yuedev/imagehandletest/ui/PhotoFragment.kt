package com.yuedev.imagehandletest.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.permissionx.guolindev.PermissionX
import com.yuedev.imagehandletest.adapter.StickerAdapter
import com.yuedev.imagehandletest.bean.Sticker
import com.yuedev.imagehandletest.databinding.FragmentPhotoBinding
import com.yuedev.imagehandletest.getStickers
import com.yuedev.imagehandletest.loadImageWithUri
import com.yuedev.imagehandletest.savePhotoWithBitmap
import com.yuedev.imagehandletest.view.StickerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initBgView()
        initView()

    }


    private fun initRecyclerView() {

        binding.stickerRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val adapter = StickerAdapter {
            val sticker = getStickers()[it]
            addSticker(sticker)
        }

        binding.stickerRecyclerView.adapter = adapter

        adapter.submitList(getStickers())

    }


    private fun initBgView() {
        val imgUri = arguments?.getParcelable<Uri>("imageUri") ?: return

        //uri加载图片google推荐在io线程
        MainScope().launch {

            binding.progressBar.visibility = View.VISIBLE
            val imageBitmap = loadImageWithUri(requireContext(), imgUri)
            binding.bgView.setImageBitmap(imageBitmap)
            binding.progressBar.visibility = View.GONE

        }
    }


    private fun initView() {
        binding.saveButton.setOnClickListener {
            getPermissionAndSavePhoto()
        }
    }


    private fun addSticker(sticker: Sticker) {
        val stickerView = StickerView(requireContext())
        binding.frameLayout.addView(stickerView)
        val bitmap = BitmapFactory.decodeResource(resources, sticker.imgResId)
        stickerView.setImageBitmap(bitmap)


        stickerView.setRectOnlyOne = {
            for (i in 1 until binding.frameLayout.childCount) {
                val view = binding.frameLayout[i] as StickerView
                if (view != it) view.setShowRect(false)
            }
        }


        stickerView.setCloseView = {
            binding.frameLayout.removeView(it)
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

        val bg = Bitmap.createBitmap(binding.bgView.width, binding.bgView.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bg)


        binding.bgView.getResultBitmap(canvas)


        for (i in 1 until binding.frameLayout.childCount) {
            val view = binding.frameLayout[i] as StickerView
            view.getResultBitmap(canvas)
        }

        savePhotoWithBitmap(requireContext(), bg, "newImg_${SystemClock.elapsedRealtime()}") {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            bg.recycle()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}