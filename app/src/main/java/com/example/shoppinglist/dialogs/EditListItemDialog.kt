package com.example.shoppinglist.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.EditListItemDialogBinding
import com.example.shoppinglist.databinding.NewListDialogBinding
import com.example.shoppinglist.entities.ShopListItem

object EditListItemDialog {
    fun showDialog(context: Context, item: ShopListItem, listener: Listener) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val binding = EditListItemDialogBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        binding.apply {
            edName.setText(item.name)
            edInfo.setText(item.itemInfo)
            if (item.itemType == 1) edInfo.visibility = View.GONE
            buttonUpdate.setOnClickListener {
                if (edName.text.toString().isNotEmpty()){
                    val itemInfo = if (edInfo.text.toString().isEmpty()) null else edInfo.text.toString()
                    listener.onClick(item.copy(name = edName.text.toString(), itemInfo = itemInfo))
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

    interface Listener {
        fun onClick(item: ShopListItem)
    }
}