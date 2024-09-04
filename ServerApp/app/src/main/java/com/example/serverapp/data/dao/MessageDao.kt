package com.example.serverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.serverapp.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Insert
    fun addNewMessage(message: Message)
    
    @Transaction
    @Query(
        """
        SELECT * FROM Message
        WHERE chatId = :conversationId
        ORDER BY timestamp ASC
    """
    )
    fun getMessagesForConversation(conversationId: Int): Flow<List<Message>>
    
    @Update
    fun updateMessage(copy: Message)
    
    @Query(
        """
        DELETE FROM Message
        WHERE messageId = :messageId
    """
    )
    fun deleteMessage(messageId: Int)
    
    @Query(
        """
    SELECT * FROM Message
    WHERE chatId = :conversationId
    ORDER BY timestamp DESC
    """
    )
    fun getLatestMessagesForConversation(conversationId: Int): Flow<List<Message>>
    
}