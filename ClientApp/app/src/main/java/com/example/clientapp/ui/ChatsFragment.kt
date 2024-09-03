package com.example.clientapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clientapp.MainViewModel
import com.example.clientapp.adapters.ConversationAdapter
import com.example.clientapp.databinding.FragmentChatsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val conversationAdapter = ConversationAdapter(viewModel, findNavController())
        binding.conversations.adapter = conversationAdapter
        binding.conversations.layoutManager = LinearLayoutManager(requireContext())
        
        lifecycleScope.launch {
            viewModel.conversationsFlow.collect { conversationList ->
                conversationAdapter.submitList(conversationList)
                Log.d("ChatsFragment", "Conversations updated: $conversationList")
            }
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSearch.setOnClickListener {
            binding.searchContainer.visibility = View.VISIBLE
            binding.ivSearch.visibility = View.GONE
        }

        binding.ivClose.setOnClickListener {
            binding.searchContainer.visibility = View.GONE
            binding.ivSearch.visibility = View.VISIBLE
        }

    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}