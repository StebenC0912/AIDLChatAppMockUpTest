package com.example.clientapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clientapp.MainViewModel
import com.example.clientapp.R
import com.example.clientapp.utils.DateUtils
import com.example.serverapp.models.Message

class MessageAdapter(
    private val currentUserId: Int,
    private val viewModel: MainViewModel,
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    
    private var deleteMode = false
    
    fun setDeleteMode(enabled: Boolean) {
        deleteMode = enabled
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if ((message.isDeletedBySender && message.senderId == currentUserId) ||
            (message.isDeletedByReceiver && message.receiverId == currentUserId)
        ) {
            VIEW_TYPE_HIDDEN
        } else if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_hidden, parent, false)
            HiddenMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message, deleteMode, viewModel)
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            (holder as ReceivedMessageViewHolder).bind(message, deleteMode, viewModel)
        }
    }
    
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)
        private val messageCheckbox: CheckBox = itemView.findViewById(R.id.message_checkbox)
        fun bind(message: Message, deleteMode: Boolean, viewModel: MainViewModel) {
            messageText.text = message.content
            messageTime.text = formatTimestamp(message.timestamp)
            messageCheckbox.visibility = if (deleteMode) View.VISIBLE else View.GONE
            messageCheckbox.isChecked = viewModel.selectedMessages.contains(message)
            
            messageCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.addMessageToSelection(message)
                } else {
                    viewModel.removeMessageFromSelection(message)
                }
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            return DateUtils.formatTimestamp(timestamp)
        }
    }
    
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)
        private val messageCheckbox: CheckBox = itemView.findViewById(R.id.message_checkbox)
        
        fun bind(message: Message, deleteMode: Boolean, viewModel: MainViewModel) {
            messageText.text = message.content
            messageTime.text = formatTimestamp(message.timestamp)
            messageCheckbox.visibility = if (deleteMode) View.VISIBLE else View.GONE
            messageCheckbox.isChecked = viewModel.selectedMessages.contains(message)
            messageCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.addMessageToSelection(message)
                } else {
                    viewModel.removeMessageFromSelection(message)
                }
            }
        }
        
        private fun formatTimestamp(timestamp: Long): String {
            return itemView.context.getString(R.string.time, DateUtils.formatTimestamp(timestamp))
        }
    }
    
    class HiddenMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_HIDDEN = 3
    }
    
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageId == newItem.messageId
    }
    
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}
