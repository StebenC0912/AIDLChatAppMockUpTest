package com.example.clientapp.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.clientapp.MainViewModel
import com.example.clientapp.R
import com.example.clientapp.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var profileImageUri: Uri =
        Uri.parse("android.resource://com.example.clientapp/drawable/default_vector_image")
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                profileImageUri = uri
                displaySelectedPhoto(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    private val screenWidth by lazy {
        resources.displayMetrics.widthPixels.toFloat()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setInitialPositions()
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateViewsIn()
        setupTextWatchers()
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            animateViewsOut()
        }
        binding.registerButton.setOnClickListener {
            if (binding.profileImageFrameLayout.isVisible) {
                handleFinalRegistration()
            } else {
                showAvatarAndNameInput()
            }
        }
        binding.profileImageView.setOnClickListener {
            launchPhotoPicker()
        }
    }
    
    private fun launchPhotoPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    
    private fun displaySelectedPhoto(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.profileImageView)
    }
    
    private fun setRegisterButtonState(isEnabled: Boolean) {
        binding.registerButton.isEnabled = isEnabled
        val backgroundRes = if (isEnabled) {
            R.drawable.circular_gradient_button_active
        } else {
            R.drawable.circular_gradient_button_not_active
        }
        binding.registerButton.background =
            ContextCompat.getDrawable(requireContext(), backgroundRes)
    }
    
    
    private fun handleFinalRegistration() {
        val name = binding.nameEditText.text.toString().trim()
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        if (name.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            viewModel.registerUser(name, username, password, profileImageUri)
            animateViewsOut()
        }
    }
    
    
    private fun showAvatarAndNameInput() {
        binding.usernameAndPasswordLinearLayout.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .withEndAction {
                binding.usernameAndPasswordLinearLayout.isVisible = false
                binding.enterYourEmailAndPasswordTextView.isVisible = false
                binding.profileImageFrameLayout.apply {
                    translationX = screenWidth
                    alpha = 0f
                    isVisible = true
                    animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                }
                
                binding.nameEditText.apply {
                    translationX = screenWidth
                    alpha = 0f
                    isVisible = true
                    animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(500)
                        .withEndAction {
                            setRegisterButtonState(binding.nameEditText.text.isNotEmpty())
                        }
                        .start()
                }
            }
            .start()
    }
    
    private fun setupTextWatchers() {
        binding.usernameEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.nameEditText.addTextChangedListener(textWatcher)
    }
    
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val name = binding.nameEditText.text.toString().trim()
            
            when {
                binding.profileImageFrameLayout.isVisible -> {
                    setRegisterButtonState(name.isNotEmpty())
                }
                
                else -> {
                    setRegisterButtonState(username.isNotEmpty() && password.isNotEmpty())
                }
            }
        }
        
        override fun afterTextChanged(s: Editable?) {}
    }
    
    private fun setInitialPositions() {
        binding.loginButton.translationX = -screenWidth
        binding.registerTextView.translationX = screenWidth
        binding.enterYourEmailAndPasswordTextView.translationX =
            screenWidth
    }
    
    private fun animateViewsIn() {
        binding.loginButton.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
        
        binding.registerTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()                // Navigate back to the login fragment
        
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(0f)
            .setDuration(500)
            .start()
    }
    
    private fun animateViewsOut() {
        binding.enterYourEmailAndPasswordTextView.animate()
            .translationX(screenWidth)
            .setDuration(500)
            .start()
        
        binding.registerTextView.animate()
            .translationX(screenWidth)
            .setDuration(500)
            .start()
        
        binding.loginButton.animate()
            .translationX(-screenWidth)
            .setDuration(500)
            .withEndAction {
                requireActivity().onBackPressed()
            }
            .start()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
