package com.example.clientapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.clientapp.R
import com.example.clientapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1001
    }

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

        binding.ivAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_error)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }
        }
    }

}