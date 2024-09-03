package com.example.serverapp.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user1Id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user2Id"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["user1Id"]), Index(value = ["user2Id"])]
)
data class Conversation(
    @PrimaryKey(autoGenerate = true) val conversationId: Int = 0,
    val user1Id: Int,
    val user2Id: Int,
    var lastMessageContent: String? = null,
    var lastMessageTimestamp: Long? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(conversationId)
        parcel.writeInt(user1Id)
        parcel.writeInt(user2Id)
        parcel.writeString(lastMessageContent)
        parcel.writeValue(lastMessageTimestamp)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }
        
        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }
    
}
