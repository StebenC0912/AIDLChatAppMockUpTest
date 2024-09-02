package com.example.serverapp.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["receiverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["conversationId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["senderId"]), Index(value = ["receiverId"])]
)
data class Message(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val chatId: Int,
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val timestamp: Long,
    val isDeletedBySender: Boolean = false,
    val isDeletedByReceiver: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readBoolean(),
        parcel.readBoolean()
    )
    
    override fun describeContents(): Int {
        return 0
    }
    
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(messageId)
        p0.writeInt(chatId)
        p0.writeInt(senderId)
        p0.writeInt(receiverId)
        p0.writeString(content)
        p0.writeLong(timestamp)
        p0.writeBoolean(isDeletedBySender)
        p0.writeBoolean(isDeletedByReceiver)
    }
    
    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }
        
        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}
