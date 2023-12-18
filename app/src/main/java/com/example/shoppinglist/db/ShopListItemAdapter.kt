package com.example.shoppinglist.db

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Paint
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ListNameItemBinding
import com.example.shoppinglist.databinding.ShopLibraryListItemBinding
import com.example.shoppinglist.databinding.ShopListItemBinding
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.entities.ShopListItem
import kotlin.math.log

class ShopListItemAdapter(private val listener: Listener) :
    ListAdapter<ShopListItem, ShopListItemAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun setItemData(shopListItem: ShopListItem, listener: Listener) {
            val binding = ShopListItemBinding.bind(view)
            binding.apply {
                tvName.text = shopListItem.name
                tvInfo.text = shopListItem.itemInfo
                tvInfo.visibility = infoVisibility(shopListItem)
                checkBox.isChecked = shopListItem.itemChecked
                setPaintFlagAndColor(binding)
                checkBox.setOnClickListener {
                   // setPaintFlagAndColor(binding)
                    listener.onClickItem(shopListItem.copy(itemChecked = checkBox.isChecked), CHECK_BOX)
                }
                imEdit.setOnClickListener {
                    listener.onClickItem(shopListItem, EDIT)
                }
            }

        }

        fun setLibraryData(shopListItem: ShopListItem, listener: Listener) {
            val binding = ShopLibraryListItemBinding.bind(view)
            binding.apply {
                tvName.text = shopListItem.name
                imEdit.setOnClickListener {
                    listener.onClickItem(shopListItem, EDIT_LIBRARY_ITEM)
                }
                imDelete.setOnClickListener {
                    listener.onClickItem(shopListItem, DELETE_LIBRARY_ITEM)
                }
                itemView.setOnClickListener {
                    listener.onClickItem(shopListItem, ADD)
                }
            }
        }

        @SuppressLint("ResourceType")
        private fun setPaintFlagAndColor(binding: ShopListItemBinding){
            binding.apply {
                if (checkBox.isChecked){
                    tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvInfo.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_secondary))
                    tvInfo.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_secondary))
                }else{
                    tvName.paintFlags = Paint.ANTI_ALIAS_FLAG
                    tvInfo.paintFlags = Paint.ANTI_ALIAS_FLAG

                    val defPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                    if (defPreferences.getString("theme_key", "light") == "light") {
                        tvName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_light_onSurface))
                        tvInfo.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_light_onSurface))
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_onSurface))
                        tvInfo.setTextColor(ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_onSurface))
                    }
                }
            }
        }

        private fun infoVisibility(shopListItem: ShopListItem): Int
        {
            return if (shopListItem.itemInfo.isNullOrEmpty()){
                View.GONE
            }else{
                View.VISIBLE
            }
        }
        companion object {

            fun createShopItem(parent: ViewGroup): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.shop_list_item, parent, false)
                )
            }

            fun createLibraryItem(parent: ViewGroup): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.shop_library_list_item, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return if (viewType == 0){
            ItemHolder.createShopItem(parent)
        } else {
            ItemHolder.createLibraryItem(parent)
        }
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (getItem(position).itemType == 0){
            holder.setItemData(getItem(position), listener)
        }else{
            holder.setLibraryData(getItem(position),listener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType
    }

    class ItemComparator : DiffUtil.ItemCallback<ShopListItem>() {
        override fun areItemsTheSame(
            oldItem: ShopListItem,
            newItem: ShopListItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ShopListItem,
            newItem: ShopListItem
        ): Boolean {
            return oldItem == newItem
        }

    }

    interface Listener {
        fun onClickItem(shopListItem: ShopListItem, state: Int)
    }

    companion object{
        const val EDIT = 0
        const val CHECK_BOX = 1
        const val EDIT_LIBRARY_ITEM = 2
        const val DELETE_LIBRARY_ITEM = 3
        const val ADD = 4
    }

}