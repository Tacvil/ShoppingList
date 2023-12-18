package com.example.shoppinglist.db

import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.NoteListItemBinding
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.utils.HtmlManager
import com.example.shoppinglist.utils.TimeManager

class NoteAdapter(private val listener: Listener, private val defPreferences: SharedPreferences) : ListAdapter<NoteItem, NoteAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(view:View): RecyclerView.ViewHolder(view){
        private val binding = NoteListItemBinding.bind(view)

                fun setData(note: NoteItem, listener: Listener, defPreferences: SharedPreferences) = with(binding){
                    if (note.title.isEmpty()){
                        textViewTitle.visibility = View.GONE
                    }else{
                        textViewTitle.text = note.title
                    }

                    if (note.context.isEmpty()){
                        textViewDescription.visibility = View.GONE
                    }else{
                        textViewDescription.text = HtmlManager.getFromHtml(note.context).trim()
                    }
                    textViewTime.text = TimeManager.getTimeFormat(note.time, defPreferences)

                    itemView.setOnClickListener {
                        listener.onClickItem(note)
                    }

                    imageButtonDelete.setOnClickListener {
                        listener.deleteItem(note.id!!)
                    }
                }

        companion object{

            fun create(parent: ViewGroup): ItemHolder{
                return ItemHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener, defPreferences)
    }

    class ItemComparator : DiffUtil.ItemCallback<NoteItem>(){
        override fun areItemsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean {
            return oldItem == newItem
        }

    }

    interface Listener{
        fun deleteItem(id: Int)
        fun onClickItem(note: NoteItem)
    }

}