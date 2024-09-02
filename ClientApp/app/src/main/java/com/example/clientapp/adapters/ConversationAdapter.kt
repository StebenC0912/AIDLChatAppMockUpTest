package com.example.clientapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clientapp.MainViewModel
import com.example.clientapp.databinding.ConversationItemBinding
import com.example.clientapp.utils.ImageConverter
import com.example.serverapp.models.Conversation

class ConversationAdapter(
    private val viewModel: MainViewModel,
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {
    class ConversationViewHolder(private val binding: ConversationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(conversation: Conversation, viewModel: MainViewModel) {
            val userId1 = conversation.user1Id
            val userId2 = conversation.user2Id
            val user = viewModel.getUserById(userId1, userId2)
            binding.name.text = user.name
            binding.messagePreview.text = conversation.lastMessageContent
            binding.timeDate.text = convertTimestampToDate(conversation.lastMessageTimestamp)
            Glide.with(binding.root).load(
                ImageConverter().stringToBitmap(
                    user.image
                )
            ).into(binding.profileImage)
        }
        
        
        private fun convertTimestampToDate(timestamp: Long?): String {
            if (timestamp == null) {
                return ""
            }
            return timestamp.toString()
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
        holder.bind(conversation, viewModel)
    }
}
