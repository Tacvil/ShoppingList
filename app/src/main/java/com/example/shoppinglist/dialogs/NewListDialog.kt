package com.example.shoppinglist.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.NewListDialogBinding

object NewListDialog {
    fun showDialog(context: Context, listener: Listener, name: String) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val binding = NewListDialogBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        binding.apply {
            edNewListName.setText(name)
            if (name.isNotEmpty()){
                buttonCreate.text = context.getString(R.string.update)
                textViewTitle.text = context.getString(R.string.update_list_name_title)
            }
            buttonCreate.setOnClickListener {
                val listName = edNewListName.text.toString()
                if (listName.isNotEmpty()){
                    listener.onClick(listName)
                }
                dialog?.dismiss()
            }
        }
        dialog = builder.create()
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        dialog.show()
    }
    interface Listener{
        fun onClick(name: String)
    }
}