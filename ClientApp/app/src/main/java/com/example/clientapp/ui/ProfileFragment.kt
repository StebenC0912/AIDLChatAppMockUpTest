package com.example.clientapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clientapp.MainViewModel
import com.example.clientapp.R
import com.example.clientapp.databinding.FragmentProfileBinding
import com.example.clientapp.utils.ImageConverter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    
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
        val currentUser = viewModel.getCurrentUser()
        binding.tvName.text = currentUser.name
        binding.tvUsername.text = currentUser.username
        Glide.with(this)
            .load(
                ImageConverter().stringToBitmap(
                    currentUser.image
                )
            )
            .placeholder(R.drawable.avatar_placeholder)
            .error(R.drawable.avatar_error)
            .circleCrop()
            .into(binding.ivAvatar)
        
        binding.btnProfileLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }
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