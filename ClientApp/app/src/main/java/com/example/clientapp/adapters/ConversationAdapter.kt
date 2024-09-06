package com.example.clientapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clientapp.MainViewModel
import com.example.clientapp.R
import com.example.clientapp.databinding.ConversationItemBinding
import com.example.clientapp.utils.DateUtils
import com.example.clientapp.utils.ImageConverter
import com.example.serverapp.models.Conversation

class ConversationAdapter(
    private val viewModel: MainViewModel,
    private val navController: NavController,
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {
    class ConversationViewHolder(private val binding: ConversationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(
            conversation: Conversation,
            viewModel: MainViewModel,
            navController: NavController,
        ) {
            val userId1 = conversation.user1Id
            val userId2 = conversation.user2Id
            val user = viewModel.getUserById(userId1, userId2)
            val lastMessage = viewModel.getVisibleLastMessage(conversation)
            binding.name.text = user.name
            binding.messagePreview.text = lastMessage.content
            binding.timeDate.text = convertTimestampToDate(lastMessage.timestamp)
            Glide.with(binding.root).load(
                ImageConverter().stringToBitmap(
                    user.image
                )
            ).into(binding.profileImage)
            binding.root.setOnClickListener {
                viewModel.saveCurrentConversation(conversation)
                navController.navigate(
                    R.id.action_mainFragment_to_conversationFragment
                )
            }
            
            binding.root.setOnLongClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle(binding.root.context.getString(R.string.delete_conversation_title))
                    .setMessage(binding.root.context.getString(R.string.delete_conversation_message))
                    .setPositiveButton(binding.root.context.getString(R.string.yes)) { _, _ ->
                        viewModel.deleteConversation(conversation)
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.conversation_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton(binding.root.context.getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                true
            }
        }
        
        
        private fun convertTimestampToDate(timestamp: Long?): String {
            if (timestamp == null || timestamp == 0L) {
                return ""
            }
            return DateUtils.formatTimestamp(timestamp)
        }
    }
    
    class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }
        
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding =
            ConversationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)
        holder.bind(conversation, viewModel, navController)
    }
}
