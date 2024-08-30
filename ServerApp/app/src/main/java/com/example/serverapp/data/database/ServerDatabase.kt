package com.example.serverapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.serverapp.data.dao.UserDao
import com.example.serverapp.models.User

@Database(entities = [User::class], version = 1)
abstract class ServerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    
    companion object {
        private const val DATABASE_NAME = "server_database"
        
        @Volatile
        private var instance: ServerDatabase? = null
        
        fun getInstance(context: Context): ServerDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    ServerDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
            return instance!!
        }
    }
}