package com.example.clientapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clientapp.MainViewModel
import com.example.clientapp.adapters.MessageAdapter
import com.example.clientapp.databinding.FragmentConversationBinding
import com.example.clientapp.utils.ImageConverter
import kotlinx.coroutines.launch

class ConversationFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        handleWindowInsetsForAndroidRAndAbove()
        showKeyboardIfNeeded()
        val currentUserId = viewModel.getUserIdFromSharedPreferences()
        binding.userName.text = viewModel.getReceiverName()
        Glide.with(binding.root).load(
            ImageConverter().stringToBitmap(
                viewModel.getReceiverImage()
            )
        ).into(binding.profileImageView)
        val conversationAdapter = MessageAdapter(currentUserId)
        binding.recyclerView.adapter = conversationAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            viewModel.sendMessage(message)
            binding.messageInput.text.clear()
        }
        
        lifecycleScope.launch {
            viewModel.messagesFlow.collect { messages ->
                conversationAdapter.submitList(messages) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
                Log.d("ConversationFragment", "Messages updated: $messages")
            }
        }
    }
    
    private fun handleWindowInsetsForAndroidRAndAbove() {
        activity?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsets.Type.ime())
            val systemGestureInsets = windowInsets.getInsets(WindowInsets.Type.systemGestures())
            
            binding.root.setPadding(0, 0, 0, imeInsets.bottom - systemGestureInsets.bottom)
            
            WindowInsets.CONSUMED
        }
    }
    
    private fun showKeyboardIfNeeded() {
        binding.messageInput.requestFocus()
        view?.post {
            activity?.let { activity ->
                WindowCompat.getInsetsController(activity.window, requireView())
                    .show(WindowInsetsCompat.Type.ime())
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}
