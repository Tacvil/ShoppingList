package com.example.shoppinglist.fragments

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.shoppinglist.R
import com.example.shoppinglist.activities.MainApp
import com.example.shoppinglist.activities.NewNoteActivity
import com.example.shoppinglist.data.SortOrder
import com.example.shoppinglist.databinding.FragmentNoteBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.db.NoteAdapter
import com.example.shoppinglist.dialogs.DeleteDialog
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.utils.FragmentManager.openFragment
import com.example.shoppinglist.utils.onQueryTextChanged


class NoteFragment : BaseFragment(), NoteAdapter.Listener, MenuProvider {

    private lateinit var binding: FragmentNoteBinding
    private lateinit var editLauncher: ActivityResultLauncher<Intent>
    private lateinit var adapter: NoteAdapter
    private lateinit var defPreferences: SharedPreferences
    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.MainViewModelFactory((context?.applicationContext as MainApp).database, requireActivity().application)
    }

    override fun onClickNew() {
        val options = activity?.let { ActivityOptionsCompat.makeCustomAnimation(it, android.R.anim.fade_in, android.R.anim.fade_out) }
        editLauncher.launch(Intent(activity, NewNoteActivity::class.java), options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onEditResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.addMenuProvider(this, viewLifecycleOwner , Lifecycle.State.RESUMED)
        binding = FragmentNoteBinding.inflate(inflater, container, false)

        mainViewModel.curFragm = 2

        mainViewModel.tasks.observe(viewLifecycleOwner) { todos ->
            val NotesItems = todos.filterIsInstance<NoteItem>()
            adapter.submitList(NotesItems)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        observer()
        mainViewModel.updateSearchInNotes(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.updateSearchInNotes(false)
    }

    private fun initRcView() = with(binding) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val spacing = resources.getDimensionPixelSize(R.dimen.item_note_margin)
        val spacingLeftRight = resources.getDimensionPixelSize(R.dimen.left_right_margin_item_note)
        val spacingDecoration = ItemSpacingDecoration(spacing, spacingLeftRight, getLayoutManager())
        recyclerViewNote.addItemDecoration(spacingDecoration)
        if (getLayoutManager() == "Linear") {
            recyclerViewNote.layoutManager = LinearLayoutManager(activity)
        } else {
            recyclerViewNote.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        adapter = NoteAdapter(this@NoteFragment, defPreferences)
        recyclerViewNote.adapter = adapter
    }

    private fun getLayoutManager(): String {
        return if (defPreferences.getString(
                "note_style_key",
                "Linear"
            ) == "Linear"
        ) "Linear" else "Grid"
    }

    private fun observer() {
        mainViewModel.allNotes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun onEditResult() {
        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val editState = it.data?.getStringExtra(EDIT_STATE_KEY)
                if (editState == "update") {
                    mainViewModel.updateNote(it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem)
                } else if(it.data?.getSerializableExtra(NEW_NOTE_KEY) != null){
                    mainViewModel.insertNote(it.data?.getSerializableExtra(NEW_NOTE_KEY) as NoteItem)
                }
            }
        }
    }

    companion object {
        const val NEW_NOTE_KEY = "new_note_key"
        const val EDIT_STATE_KEY = "edit_state_key"

        @JvmStatic
        fun newInstance() = NoteFragment()
    }

    override fun deleteItem(id: Int) {
        DeleteDialog.showDialog(context as AppCompatActivity, object : DeleteDialog.Listener {
            override fun onClick() {
                mainViewModel.deleteNote(id)
            }
        })
    }

    override fun onClickItem(note: NoteItem) {
        val options = activity?.let { ActivityOptionsCompat.makeCustomAnimation(it, android.R.anim.fade_in, android.R.anim.fade_out) }

        val intent = Intent(activity, NewNoteActivity::class.java).apply {
            putExtra(NEW_NOTE_KEY, note)
        }
        editLauncher.launch(intent, options)
    }

    class ItemSpacingDecoration(private val spacing: Int, private val spacingLeftRight: Int, private val layout: String) :
        RecyclerView.ItemDecoration() {

        // Переопределяем метод getItemOffsets для установки отступов
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)


            // Применяем отступы только для элементов, кроме первого
            if (layout == "Linear") {
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.top = spacing // Устанавливаем верхний отступ
                }
            } else {
                if (parent.getChildAdapterPosition(view) != 0 && parent.getChildAdapterPosition(view) != 1) {
                    outRect.top = spacing // Устанавливаем верхний отступ
                }

                val position = parent.getChildAdapterPosition(view)
                val layoutManager = parent.layoutManager as StaggeredGridLayoutManager
                val spanCount = layoutManager.spanCount
                val column = position % spanCount

                // Устанавливаем отступы в зависимости от столбца
                if (column == 0) {
                  //  outRect.left = leftSpacing1
                    outRect.right = spacingLeftRight
                } else {
                    outRect.left = spacingLeftRight
                   // outRect.right = rightSpacing2
                }

            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_list, menu)

        val search = menu.findItem(R.id.todo_search)
        val searchView = search.actionView as SearchView

        searchView.onQueryTextChanged { querySearch ->
            mainViewModel.searchQuery.value = querySearch
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.sort_by_name -> {
                mainViewModel.onSortOrderSelected(SortOrder.BY_NAME, 2)
            }

            R.id.sort_by_date -> {
                mainViewModel.onSortOrderSelected(SortOrder.BY_DATE, 2)
            }
            R.id.del_all_tasks -> {
                deleteAllTasks()
            }
        }
        return true
    }

    private fun deleteAllTasks() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_->
            mainViewModel.deleteAllNotes()
            Toast.makeText(requireContext(), "All notes lists have been successfully deleted!", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("No") {_,_-> }
        builder.setTitle("Confirm Deletion")
        builder.setIcon(R.drawable.ic_warning)
        builder.setMessage("Are you sure you want to delete all notes?")
        builder.create().show()
    }

}