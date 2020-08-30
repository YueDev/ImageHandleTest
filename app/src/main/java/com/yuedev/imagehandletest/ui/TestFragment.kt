package com.yuedev.imagehandletest.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.loadImageWithUri
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch



//测试自定义的手势检测
//MyScaleView以及MyGestureDetctor
class TestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

    }


    private fun initView() {

        val imgUri = requireArguments().getParcelable<Uri>("imageUri") ?: return

        MainScope().launch {
            val bitmap = loadImageWithUri(requireContext(), imgUri)
            scaleView.setImageBitmap(bitmap)
        }




    }


}