package com.example.shoppinglist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shoppinglist.entities.LibraryItem
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.entities.ShopListItem
import com.example.shoppinglist.entities.ShopListNameItem

@Database (entities = [LibraryItem::class, NoteItem::class, ShopListItem::class, ShopListNameItem::class], version = 1)
@TypeConverters(NoteTypeConverters::class)
abstract class MainDataBase: RoomDatabase() {

    abstract fun getDao(): Dao

    companion object{

        @Volatile
        private var INSTANCE : MainDataBase? = null

        fun getDataBase(context: Context):MainDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDataBase::class.java,
                    "shopping_list.db"
                ).build()
                instance
            }
        }

    }

}