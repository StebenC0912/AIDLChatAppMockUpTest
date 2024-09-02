package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.clientapp.databinding.FragmentEditProfileBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEditProfileBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            // Handle save button click
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
