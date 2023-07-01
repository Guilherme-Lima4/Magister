package com.example.magister

import android.os.Parcel
import android.os.Parcelable

class ChatMessage(
    var text: String? = "",
    var timestamp: Long = 0,
    var fromId: String? = "",
    var toId: String? = "",
    var conversationId: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeLong(timestamp)
        parcel.writeString(fromId)
        parcel.writeString(toId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatMessage> {
        override fun createFromParcel(parcel: Parcel): ChatMessage {
            return ChatMessage(parcel.readString(), parcel.readLong(), parcel.readString(), parcel.readString())
        }

        override fun newArray(size: Int): Array<ChatMessage?> {
            return arrayOfNulls(size)
        }
    }
}
