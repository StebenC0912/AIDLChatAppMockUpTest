package com.example.serverapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.serverapp.data.dao.ConversationDao
import com.example.serverapp.data.dao.MessageDao
import com.example.serverapp.data.dao.UserDao
import com.example.serverapp.data.utils.TypeConverter
import com.example.serverapp.models.Conversation
import com.example.serverapp.models.Message
import com.example.serverapp.models.User

@Database(entities = [User::class, Message::class, Conversation::class], version = 1)
@TypeConverters(TypeConverter::class)
abstract class ServerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        private const val DATABASE_NAME = "server_database"
        
        @Volatile
        private var instance: ServerDatabase? = null
        
        fun getInstance(context: Context): ServerDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, ServerDatabase::class.java, DATABASE_NAME
                    ).build()
                }
            }
            return instance!!
        }
    }
}
