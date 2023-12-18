package com.example.shoppinglist.db

import androidx.room.TypeConverter
import java.util.*

/** Our type Converters file. **/

class NoteTypeConverters {

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch : Long?) : Date ? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }
}