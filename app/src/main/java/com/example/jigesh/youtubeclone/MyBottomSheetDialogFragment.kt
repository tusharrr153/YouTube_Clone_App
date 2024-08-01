package com.example.jigesh.youtubeclone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jigesh.youtubeclone.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MyBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = BottomSheetLayoutBinding.inflate(inflater, container, false)

        binding.createashort.setOnClickListener {
            startActivity(Intent(requireContext(), CreateShortActivity::class.java))
        }

        binding.uploadavideo.setOnClickListener {
            startActivity(Intent(requireContext(), CreateVideoActivity::class.java))
        }

        binding.cancelButton.setOnClickListener {
            dismiss()  // Close the bottom sheet dialog
        }


        return binding.root
    }
}
