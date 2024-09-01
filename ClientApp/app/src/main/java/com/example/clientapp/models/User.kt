package com.example.serverapp.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "image") val image: Uri,
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        Uri.parse(parcel.readString())
    )
    
    
    override fun describeContents(): Int {
        return 0
    }
    
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(id)
        p0.writeString(name)
        p0.writeString(username)
        p0.writeString(password)
        p0.writeString(image.toString())
    }
    
    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }
        
        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
