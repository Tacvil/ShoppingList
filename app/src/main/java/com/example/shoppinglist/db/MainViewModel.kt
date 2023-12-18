package com.example.shoppinglist.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.FilterPreferences
import com.example.shoppinglist.data.PreferencesManager
import com.example.shoppinglist.data.SortOrder
import com.example.shoppinglist.entities.LibraryItem
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.entities.ShopListItem
import com.example.shoppinglist.entities.ShopListNameItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(dataBase: MainDataBase, application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    var curFragm : Int = 0
    val searchQuery = MutableStateFlow("")
    val preferencesFlow = preferencesManager.preferencesFlow
    val dao = dataBase.getDao()

    val tasksFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        Triple(
            query,
            when (curFragm) {
                1 -> filterPreferences.sortOrder
                2 -> filterPreferences.sortOrder2
                else -> filterPreferences.sortOrder
            },
            filterPreferences.searchInNotes
        )
    }
        .flatMapLatest { (query, sortOrder, searchInNotes) ->
            when (sortOrder) {
                SortOrder.BY_DATE -> {
                    if (searchInNotes) {
                        dao.getAllNotes(query, SortOrder.BY_DATE)
                    } else {
                        dao.getAllTasks(query, SortOrder.BY_DATE)
                    }
                }
                SortOrder.BY_NAME -> {
                    if (searchInNotes) {
                        dao.getAllNotes(query, SortOrder.BY_NAME)
                    } else {
                        dao.getAllTasks(query, SortOrder.BY_NAME)
                    }
                }
            }
        }
        .map { tasks ->
            tasks.map { task ->
                if (task is ShopListNameItem) {
                    task
                } else if (task is NoteItem) {
                    task
                    // В противном случае выполнить преобразование из Serializable в ShopListNameItem
                    // Например: ShopListNameItem(task.id, task.name, ...)
                } else {
                    task
                }
            }
        }

    fun onSortOrderSelected(sortOrder: SortOrder, curFrag: Int) =
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOrder, curFrag)
        }

    fun updateSearchInNotes(newValue: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSearchInNotes(newValue)
        }
    }

    val tasks = tasksFlow.asLiveData()

    val libraryItems = MutableLiveData<List<LibraryItem>>()
    val allNotes: LiveData<List<NoteItem>> = dao.getAllNotes().asLiveData()
    val allItemShopListNames: LiveData<List<ShopListNameItem>> =
        dao.getAllShopListNames().asLiveData()

    fun getAllLibraryItems(name: String) = viewModelScope.launch {
        libraryItems.postValue(dao.getAllLibraryItems(name))
    }

    fun getAllItemsFromList(listId: Int): LiveData<List<ShopListItem>> {
        return dao.getAllShopListItems(listId).asLiveData()
    }

    fun insertNote(note: NoteItem) = viewModelScope.launch {
        dao.insertNote(note)
    }

    fun insertShopListName(listName: ShopListNameItem) = viewModelScope.launch {
        dao.insertShopListName(listName)
    }

    fun insertShopItem(shopListItem: ShopListItem) = viewModelScope.launch {
        dao.insertItem(shopListItem)
        if (!isLibraryItemExists(shopListItem.name)) dao.insertLibraryItem(
            LibraryItem(
                null,
                shopListItem.name
            )
        )
    }

    fun updateNote(note: NoteItem) = viewModelScope.launch {
        dao.updateNote(note)
    }

    fun updateListItem(item: ShopListItem) = viewModelScope.launch {
        dao.updateListItem(item)
    }

    fun updateListName(shopListNameItem: ShopListNameItem) = viewModelScope.launch {
        dao.updateListName(shopListNameItem)
    }

    fun updateLibraryItem(item: LibraryItem) = viewModelScope.launch {
        dao.updateLibraryItem(item)
    }

    fun deleteNote(id: Int) = viewModelScope.launch {
        dao.deleteNote(id)
    }

    fun deleteAllNotes() = viewModelScope.launch {
        dao.deleteAllNotes()
    }

    fun deleteLibraryItem(id: Int) = viewModelScope.launch {
        dao.deleteLibraryItem(id)
    }

    fun deleteShopList(id: Int, deleteList: Boolean) = viewModelScope.launch {
        if (deleteList) dao.deleteShopListName(id)
        dao.deleteShopItemsByListId(id)
    }

    fun deleteAllShopListName() = viewModelScope.launch {
        dao.deleteAllShopListName()
    }

    private suspend fun isLibraryItemExists(name: String): Boolean {
        return dao.getAllLibraryItems(name).isNotEmpty()
    }

    class MainViewModelFactory(private val dataBase: MainDataBase, private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(dataBase, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModelClass")
        }
    }

}