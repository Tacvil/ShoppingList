package com.example.shoppinglist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.shoppinglist.data.SortOrder
import com.example.shoppinglist.entities.LibraryItem
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.entities.ShopListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    fun getAllTasks(query: String, sortOrder: SortOrder): Flow<List<ShopListNameItem>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query)
            SortOrder.BY_NAME -> getTasksSortedByName(query)
        }

    // Дополнительная функция для поиска записок
    fun getAllNotes(query: String, sortOrder: SortOrder): Flow<List<NoteItem>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getNotesSortedByDateCreated(query)
            SortOrder.BY_NAME -> getNotesSortedByName(query)
        }

    @Query("SELECT * FROM shopping_list_names WHERE name LIKE '%' || :searchQuery || '%' ORDER BY isStarted DESC, name COLLATE NOCASE")
    fun getTasksSortedByName(searchQuery: String): Flow<List<ShopListNameItem>>

    @Query("SELECT * FROM shopping_list_names WHERE name LIKE '%' || :searchQuery || '%' ORDER BY isStarted DESC, time")
    fun getTasksSortedByDateCreated(searchQuery: String): Flow<List<ShopListNameItem>>

    @Query("SELECT * FROM note_list WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title COLLATE NOCASE")
    fun getNotesSortedByName(searchQuery: String): Flow<List<NoteItem>>

    @Query("SELECT * FROM note_list WHERE title LIKE '%' || :searchQuery || '%' ORDER BY time")
    fun getNotesSortedByDateCreated(searchQuery: String): Flow<List<NoteItem>>

    @Query("SELECT * FROM note_list")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM shopping_list_names")
    fun getAllShopListNames(): Flow<List<ShopListNameItem>>

    @Query("SELECT * FROM shop_list_item WHERE listId LIKE :listId")
    fun getAllShopListItems(listId : Int): Flow<List<ShopListItem>>

    @Query("DELETE FROM note_list WHERE id IS :id")
    suspend fun deleteNote(id: Int)

    @Query("DELETE FROM library WHERE id IS :id")
    suspend fun deleteLibraryItem(id: Int)

    @Query("DELETE FROM shopping_list_names WHERE id IS :id")
    suspend fun deleteShopListName(id: Int)

    @Query("DELETE FROM shopping_list_names")
    suspend fun deleteAllShopListName()

    @Query("DELETE FROM note_list")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM shop_list_item WHERE listId LIKE :listId")
    suspend fun deleteShopItemsByListId(listId : Int)

    @Query("SELECT * FROM library WHERE name LIKE :name")
    suspend fun getAllLibraryItems(name : String): List<LibraryItem>

    @Insert
    suspend fun insertNote(note: NoteItem)

    @Insert
    suspend fun insertItem(shopListItem: ShopListItem)

    @Insert
    suspend fun insertShopListName(name: ShopListNameItem)

    @Insert
    suspend fun insertLibraryItem(libraryItem: LibraryItem)

    @Update
    suspend fun updateNote(note: NoteItem)

    @Update
    suspend fun updateLibraryItem(item: LibraryItem)

    @Update
    suspend fun updateListItem(item: ShopListItem)

    @Update
    suspend fun updateListName(shopListNameItem: ShopListNameItem)

}