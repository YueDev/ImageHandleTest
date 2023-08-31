package com.yuedev.imagehandletest.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yuedev.imagehandletest.databinding.FragmentTestBinding
import com.yuedev.imagehandletest.loadImageWithUri
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


//测试自定义的手势检测
//MyScaleView以及MyGestureDetctor
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

    }


    private fun initView() {

        val imgUri = requireArguments().getParcelable<Uri>("imageUri") ?: return

        MainScope().launch {
            val bitmap = loadImageWithUri(requireContext(), imgUri)
            binding.scaleView.setImageBitmap(bitmap)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}