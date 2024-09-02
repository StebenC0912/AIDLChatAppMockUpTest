package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.clientapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditProfileBottomSheetFragment : BottomSheetDialogFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile_bottom_sheet, container, false)
    }
    
}