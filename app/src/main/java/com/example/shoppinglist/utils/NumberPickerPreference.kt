package com.example.shoppinglist.utils

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.example.shoppinglist.R

class NumberPickerPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var numberPicker: NumberPicker? = null
    private var minValue: Int = 14
    private var maxValue: Int = 24

    init {
        widgetLayoutResource = R.layout.dialog_number_picker

    }
    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)

        // Обновляем summary при открытии настроек,
        // используя сохраненное ранее значение Preference
        val value = getPersistedInt(3)

        //val value = getPersistedString("")
        summary = value.toString()
    }

    override fun persistString(value: String?): Boolean {
        // Сохраняем выбранное значение Preference
        val persisted = super.persistString(value)

        // Обновляем summary после сохранения значения
        summary = value
        return persisted
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        numberPicker = holder.findViewById(R.id.number_picker) as NumberPicker?

        numberPicker?.scrollBarSize = 0

        numberPicker?.minValue = minValue
        numberPicker?.maxValue = maxValue

        // Set up any additional configuration for the NumberPicker

        // Set the current value
        val selectedValue = getPersistedInt(minValue)
        numberPicker?.value = selectedValue



// Найти настройку с помощью ключа
//        val sdsds = sharedPreferences?.getInt(key, 12)
//        sharedPreferences?.getString(sdsds.toString(), "12")

        // Update the value when it changes
        numberPicker?.setOnValueChangedListener { _, _, newValue ->
            summary = numberPicker?.value.toString()
            persistInt(newValue)
        }

    }

    override fun onClick() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_number_picker, null)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        numberPicker?.scrollBarSize = 0
        numberPicker?.minValue = minValue
        numberPicker?.maxValue = maxValue

        val selectedValue = getPersistedInt(minValue)
        numberPicker?.value = selectedValue

        // Update the value when it changes
        numberPicker?.setOnValueChangedListener { _, _, newValue ->
            persistInt(newValue)
        }

        AlertDialog.Builder(context)
            .setTitle("Выберите число")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                // Обработка выбранного значения
                val selectedValue = numberPicker.value
                summary = selectedValue.toString()
                // Сохранение значения в настройках
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                sharedPreferences.edit().putInt("my_number_setting", selectedValue).apply()

            }
            .setNegativeButton("Отмена", null)
            .show()
    }

}
