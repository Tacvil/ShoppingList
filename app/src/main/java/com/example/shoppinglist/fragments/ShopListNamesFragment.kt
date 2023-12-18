package com.example.shoppinglist.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.shoppinglist.R
import com.example.shoppinglist.activities.MainApp
import com.example.shoppinglist.activities.NewNoteActivity
import com.example.shoppinglist.activities.ShopListActivity
import com.example.shoppinglist.data.SortOrder
import com.example.shoppinglist.databinding.FragmentShopListNamesBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.db.ShopListNameAdapter
import com.example.shoppinglist.dialogs.DeleteDialog
import com.example.shoppinglist.dialogs.NewListDialog
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.utils.TimeManager.getCurrentTime
import com.example.shoppinglist.utils.onQueryTextChanged
import kotlinx.coroutines.launch

class ShopListNamesFragment : BaseFragment(), ShopListNameAdapter.Listener, MenuProvider {

    private lateinit var binding: FragmentShopListNamesBinding
    private lateinit var adapter: ShopListNameAdapter

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.MainViewModelFactory(
            (context?.applicationContext as MainApp).database,
            application = requireActivity().application
        )
    }

    override fun onClickNew() {
        NewListDialog.showDialog(activity as AppCompatActivity, object : NewListDialog.Listener {
            override fun onClick(name: String) {
                val shopListNameItem = ShopListNameItem(
                    null,
                    name,
                    getCurrentTime(),
                    0,
                    0,
                    false,
                    null
                )
                mainViewModel.insertShopListName(shopListNameItem)
            }

        }, "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding = FragmentShopListNamesBinding.inflate(inflater, container, false)

        mainViewModel.curFragm = 1

        mainViewModel.tasks.observe(viewLifecycleOwner) { todos ->
            val shopListItems = todos.filterIsInstance<ShopListNameItem>()
            adapter.submitList(shopListItems)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        observer()
    }

    private fun initRcView() = with(binding) {
        val spacing = resources.getDimensionPixelSize(R.dimen.item_note_margin)
        val spacingLeftRight = resources.getDimensionPixelSize(R.dimen.left_right_margin_item_note)
        val spacingDecoration = ItemSpacingDecorationList(spacing, spacingLeftRight)
        rcView.addItemDecoration(spacingDecoration)
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = ShopListNameAdapter(this@ShopListNamesFragment)
        rcView.adapter = adapter
    }

    class ItemSpacingDecorationList(private val spacing: Int, private val spacingLeftRight: Int) :
        RecyclerView.ItemDecoration() {

        // Переопределяем метод getItemOffsets для установки отступов
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            if (parent.getChildAdapterPosition(view) != 0) {
                outRect.top = spacing // Устанавливаем верхний отступ
            }
        }
    }

    private fun observer() {
        mainViewModel.allItemShopListNames.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    companion object {
        const val NEW_NOTE_KEY0 = "new_note_key"
        const val EDIT_STATE_KEY0 = "edit_state_key"

        @JvmStatic
        fun newInstance() = ShopListNamesFragment()
    }

    override fun deleteItem(id: Int) {
        DeleteDialog.showDialog(context as AppCompatActivity, object : DeleteDialog.Listener {
            override fun onClick() {
                mainViewModel.deleteShopList(id, true)
            }
        })
    }

    override fun editItem(shopListNameItem: ShopListNameItem) {

        NewListDialog.showDialog(activity as AppCompatActivity, object : NewListDialog.Listener {
            override fun onClick(name: String) {

                mainViewModel.updateListName(shopListNameItem.copy(name = name))
            }

        }, shopListNameItem.name)

    }


    override fun onClickItem(shopListNameItem: ShopListNameItem) {

        val options = activity?.let {
            ActivityOptionsCompat.makeCustomAnimation(
                it,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
        val animationBundle: Bundle? = options?.toBundle()

        val i = Intent(activity, ShopListActivity::class.java).apply {
            putExtra(ShopListActivity.SHOP_LIST_NAME, shopListNameItem)
        }
        startActivity(i, animationBundle)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_list, menu)

        val search = menu.findItem(R.id.todo_search)
        val searchView = search.actionView as SearchView

        searchView.onQueryTextChanged { querySearch ->
            mainViewModel.searchQuery.value = querySearch
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.sort_by_name -> {
                mainViewModel.onSortOrderSelected(SortOrder.BY_NAME, 1)
            }

            R.id.sort_by_date -> {
                mainViewModel.onSortOrderSelected(SortOrder.BY_DATE, 1)
            }

            R.id.del_all_tasks -> {
                deleteAllTasks()
            }
        }
        return true
    }

    private fun deleteAllTasks() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mainViewModel.deleteAllShopListName()
            Toast.makeText(
                requireContext(),
                "All shopping lists have been successfully deleted!",
                Toast.LENGTH_LONG
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Confirm Deletion")
        builder.setIcon(R.drawable.ic_warning)
        builder.setMessage("Are you sure you want to delete all shopping lists?")
        builder.create().show()
    }

}
