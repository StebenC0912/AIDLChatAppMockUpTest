package com.example.clientapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
        
        setupUI()
        setupRecyclerView()
        observeMessages()
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.deleteMenu.setOnClickListener {
            viewModel.toggleDeleteMode()
            updateUIForDeleteMode(viewModel.deleteMode)
        }
        binding.cancelButton.setOnClickListener {
            viewModel.clearSelectedMessages()
            viewModel.toggleDeleteMode()
            updateUIForDeleteMode(viewModel.deleteMode)
        }
        binding.deleteButton.setOnClickListener {
            viewModel.deleteSelectedMessages()
            viewModel.toggleDeleteMode()
            updateUIForDeleteMode(viewModel.deleteMode)
        }
    }
    
    override fun onResume() {
        super.onResume()
        scrollToBottom()
    }
    
    private fun updateUIForDeleteMode(isDeleteMode: Boolean) {
        val adapter = binding.recyclerView.adapter as MessageAdapter
        adapter.setDeleteMode(isDeleteMode)
        binding.hiddenMenu.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
    }
    
    private fun setupUI() {
        adjustWindowInsets()
        showKeyboardIfNeeded()
        loadReceiverInfo()
    }
    
    private fun adjustWindowInsets() {
        activity?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsets.Type.ime())
            val systemGestureInsets = windowInsets.getInsets(WindowInsets.Type.systemGestures())
            scrollToBottom()
            binding.root.setPadding(0, 0, 0, imeInsets.bottom - systemGestureInsets.bottom)
            WindowInsets.CONSUMED
        }
    }
    
    private fun loadReceiverInfo() {
        binding.userName.text = viewModel.getReceiverName()
        Glide.with(binding.root).load(ImageConverter().stringToBitmap(viewModel.getReceiverImage()))
            .into(binding.profileImageView)
    }
    
    private fun setupRecyclerView() {
        val conversationAdapter = MessageAdapter(viewModel.getUserId(), viewModel)
        binding.recyclerView.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeMessages() {
        lifecycleScope.launch {
            viewModel.messagesFlow.collect { messages ->
                (binding.recyclerView.adapter as MessageAdapter).submitList(messages) {
                    scrollToBottom()
                }
            }
        }
    }
    
    private fun scrollToBottom() {
        binding.recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if ((binding.recyclerView.adapter as MessageAdapter).itemCount > 0) {
                    binding.recyclerView.scrollToPosition((binding.recyclerView.adapter as MessageAdapter).itemCount - 1)
                }
            }
        })
    }
    
    
    private fun sendMessage() {
        val message = binding.messageInput.text.toString()
        if (message.isNotBlank()) {
            viewModel.sendMessage(message)
            binding.messageInput.text.clear()
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
        resetSoftInputMode()
    }
    
    private fun resetSoftInputMode() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}
