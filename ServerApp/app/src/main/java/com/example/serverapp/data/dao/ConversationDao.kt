package com.example.serverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.serverapp.models.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    
    @Insert
    fun addConversation(conversation: Conversation)
    
    @Transaction
    @Query(
        """
        SELECT * FROM Conversation
        WHERE user1Id = :userId OR user2Id = :userId
    """
    )
    fun getAllConversationsForUser(userId: Int): Flow<List<Conversation>>
}