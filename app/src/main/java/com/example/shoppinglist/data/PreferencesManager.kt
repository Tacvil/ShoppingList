package com.example.shoppinglist.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** This is our PreferenceManager in which we will persist the state of our sort order and hideCompleted by storing it in a DataStore. **/


private const val TAG = "PreferencesManager"

// Creating an Instance of our dataSore and giving it a name "user_preferences"
private val Context.dataStore by preferencesDataStore("user_preferences")

enum class SortOrder { BY_DATE, BY_NAME }
data class FilterPreferences(val sortOrder: SortOrder, val sortOrder2: SortOrder, val hideCompleted: Boolean, val searchInNotes: Boolean)

@Singleton
class PreferencesManager @Inject constructor(context : Context) {
    private val dataStore = context.dataStore

    val preferencesFlow = dataStore.data  // retrieving our data from our dataStore
        // in case of an error. Will catch the exception and the map block will use the default values(else if it's a different type of exception, will throw it)
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences",exception )
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKey.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val sortOrder2 = SortOrder.valueOf(
                preferences[PreferencesKey.SORT_ORDER2] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKey.HIDE_COMPLETED] ?: false
            val searchInNotes = preferences[PreferencesKey.SEARCH_IN_NOTES] ?: false
            FilterPreferences(sortOrder, sortOrder2, hideCompleted, searchInNotes)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder, curFrag: Int) =
        if (curFrag == 1) {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.SORT_ORDER] = sortOrder.name
            }
        } else{
            dataStore.edit { preferences ->
                preferences[PreferencesKey.SORT_ORDER2] = sortOrder.name
            }
        }

    suspend fun updateSearchInNotes(searchInNotes: Boolean) =
        dataStore.edit { preferences ->
            preferences[PreferencesKey.SEARCH_IN_NOTES] = searchInNotes
        }

    // Our preferencesKeys is how we identify a value we want to store in our jetpack dataStore
    private object PreferencesKey {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SORT_ORDER2 = stringPreferencesKey("sort_order2")
        val  HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
        val SEARCH_IN_NOTES = booleanPreferencesKey("search_in_notes")
    }
}
