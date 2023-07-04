package com.example.magister

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "buscar")
class BuscarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idSourceUser: String?,
    val idTargetUser: String?,
    val nameUserTarget: String?,
    val imageUserTarget: String?,
    val materia1: String?,
    val materia2: String?,
    val materia3: String?,
    val nomeEscolaUser: String?,
    val LastMessage: Long? = null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(idSourceUser)
        parcel.writeString(idTargetUser)
        parcel.writeString(nameUserTarget)
        parcel.writeString(imageUserTarget)
        parcel.writeString(materia1)
        parcel.writeString(materia2)
        parcel.writeString(materia3)
        parcel.writeString(nomeEscolaUser)
        parcel.writeValue(LastMessage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BuscarEntity> {
        var imageUserTarget: String? = null

        var nameUserTarget: String? = null
        var idTargetUser: String? = null


        override fun createFromParcel(parcel: Parcel): BuscarEntity {
            return BuscarEntity(parcel)
        }

        override fun newArray(size: Int): Array<BuscarEntity?> {
            return arrayOfNulls(size)
        }
    }
}