package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.clientapp.R
import com.example.clientapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditProfile.setOnClickListener {
            val bottomSheet = EditProfileBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "EditProfileBottomSheet")
        }

        Glide.with(this)
            .load(R.drawable.jack)
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_error)
            .circleCrop()
            .into(binding.ivAvatar)
    }

}