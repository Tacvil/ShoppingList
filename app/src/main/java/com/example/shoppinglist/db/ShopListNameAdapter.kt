package com.example.shoppinglist.db

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ListNameItemBinding
import com.example.shoppinglist.entities.ShopListNameItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopListNameAdapter(private val listener: Listener) : ListAdapter<ShopListNameItem, ShopListNameAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(view:View): RecyclerView.ViewHolder(view){
        private val binding = ListNameItemBinding.bind(view)

                fun setData(shopListNameItem: ShopListNameItem, listener: Listener) = with(binding){
                    textViewListName.text = shopListNameItem.name
                    textViewTime.text = shopListNameItem.time

                    progressBar.max = shopListNameItem.allItemCounter
                    progressBar.progress = shopListNameItem.checkedItemCounter
                    val colorState = ColorStateList.valueOf(getProgressColorState(shopListNameItem, binding.root.context))
                    progressBar.progressTintList = colorState
                    //counterCard.backgroundTintList = colorState
                    val counterText = "${shopListNameItem.checkedItemCounter}/${shopListNameItem.allItemCounter}"
                    textViewCounter.text = counterText

                    tvResultsReminder.isVisible = shopListNameItem.isStarted
                    reminderIcon.isVisible = shopListNameItem.isStarted
                    if (shopListNameItem.isStarted) {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val formattedTime = shopListNameItem.reminderData?.let { Date(it.time) }
                            ?.let { timeFormat.format(it) }

                        val relativeDate = shopListNameItem.reminderData?.let {
                            DateUtils.getRelativeDateTimeString(
                                binding.root.context,
                                it.time,
                                DateUtils.DAY_IN_MILLIS,
                                DateUtils.WEEK_IN_MILLIS,
                                0
                            )
                        }

                        // Убираем время и AM/PM из relativeDate
                        val cleanRelativeDate =
                            relativeDate?.replaceFirst(", \\d{1,2}:\\d{2} (AM|PM)".toRegex(), "")?.trim()

                        val dateTimeString = "$cleanRelativeDate $formattedTime"
                        tvResultsReminder.text = dateTimeString
                    }

                    // will change the color of the text if the time is Overdue.
                    val date = Date()
                    if (shopListNameItem.isStarted && shopListNameItem.reminderData?.time!! < date.time) {
                        tvResultsReminder.setTextColor(ContextCompat.getColor(binding.root.context, R.color.picker_red))
                        reminderIcon.setColorFilter(binding.root.context.resources.getColor(R.color.picker_red))

                    }

                    itemView.setOnClickListener {
                        listener.onClickItem(shopListNameItem)
                    }

                    imageButtonDelete.setOnClickListener {
                        listener.deleteItem(shopListNameItem.id!!)
                    }

                    imageButtonEdit.setOnClickListener {
                        listener.editItem(shopListNameItem)
                    }
                }

        private fun getProgressColorState(item: ShopListNameItem, context: Context): Int{
            return if (item.checkedItemCounter == item.allItemCounter){
                ContextCompat.getColor(context, R.color.md_theme_dark_surfaceTint)
            }else{
                ContextCompat.getColor(context, R.color.md_theme_light_error)
            }
        }

        companion object{

            fun create(parent: ViewGroup): ItemHolder{
                return ItemHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.list_name_item, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener)
    }

    class ItemComparator : DiffUtil.ItemCallback<ShopListNameItem>(){
        override fun areItemsTheSame(oldItem: ShopListNameItem, newItem: ShopListNameItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShopListNameItem, newItem: ShopListNameItem): Boolean {
            return oldItem == newItem
        }

    }

    interface Listener{
        fun deleteItem(id: Int)
        fun editItem(shopListNameItem: ShopListNameItem)
        fun onClickItem(shopListNameItem: ShopListNameItem)
    }

}