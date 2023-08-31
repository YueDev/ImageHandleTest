package com.yuedev.imagehandletest.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.yuedev.imagehandletest.R
import com.yuedev.imagehandletest.databinding.FragmentMainBinding


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {

            loadPhoto(100)

        }


        //测试自定义的手势检测
        binding.buttonTest.setOnClickListener {

            loadPhoto(101)

        }

    }


    //saf读取图片
    private fun loadPhoto(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, requestCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {

            data?.data?.let {
                val bundle = bundleOf("imageUri" to it)
                findNavController().navigate(R.id.action_mainFragment_to_photoFragment, bundle)
            }
        }

        if (requestCode == 101 && resultCode == RESULT_OK) {
            data?.data?.let {
                val bundle = bundleOf("imageUri" to it)
                findNavController().navigate(R.id.action_mainFragment_to_testFragment, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
