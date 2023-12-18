package com.example.shoppinglist.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "shopping_list_names")
data class ShopListNameItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "time")
    val time: String,

    @ColumnInfo(name = "allItemCounter")
    val allItemCounter: Int,

    @ColumnInfo(name = "checkedItemCounter")
    val checkedItemCounter: Int,

    @ColumnInfo(name = "isStarted")
    var isStarted: Boolean,

    @ColumnInfo(name = "reminderData")
    var reminderData: Date? = Date()

) : Serializable
