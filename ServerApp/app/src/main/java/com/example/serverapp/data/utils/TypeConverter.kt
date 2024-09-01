package com.example.serverapp.data.utils

import android.net.Uri
import androidx.room.TypeConverter

class TypeConverter {
    
    @TypeConverter
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }
    
    @TypeConverter
    fun toUri(uri: String): Uri {
        return Uri.parse(uri)
    }
}