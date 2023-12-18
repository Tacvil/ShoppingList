package com.example.shoppinglist.activities

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityMainBinding
import com.example.shoppinglist.dialogs.NewListDialog
import com.example.shoppinglist.fragments.NoteFragment
import com.example.shoppinglist.fragments.ShopListNamesFragment
import com.example.shoppinglist.settings.SettingsActivity
import com.example.shoppinglist.utils.FragmentManager
import com.example.shoppinglist.utils.FragmentManager.openActivity
import com.example.shoppinglist.utils.FragmentManager.openFragment

class MainActivity : AppCompatActivity(), NewListDialog.Listener {

    lateinit var binding: ActivityMainBinding
    private lateinit var defPreferences: SharedPreferences
    private var currentMenuItemId = R.id.shop_list
    private var currentTheme = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentTheme = defPreferences.getString("theme_key", "light").toString()
        Log.d("MyLog", "MainActivity open 1")
        openFragment(ShopListNamesFragment.newInstance(), this)
        setBottomNavListener()

        binding.floatingActionButton.setOnClickListener {
            FragmentManager.currentFrag?.onClickNew()
            //NewListDialog.showDialog(this, this)
        }

    }

    private fun setBottomNavListener(){
        binding.bottomNavView.setOnItemSelectedListener {
            when (it.itemId){
                R.id.settings ->{
                    openActivity(SettingsActivity::class.java)
                }
                R.id.notes ->{
                    currentMenuItemId = R.id.notes
                    openFragment(NoteFragment.newInstance(), this)
                }
                R.id.shop_list ->{
                    currentMenuItemId = R.id.shop_list
                    openFragment(ShopListNamesFragment.newInstance(), this)
                }
//                R.id.floatingActionButton ->{
//                    FragmentManager.currentFrag?.onClickNew()
//                    //NewListDialog.showDialog(this, this)
//                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavView.selectedItemId = currentMenuItemId
        if (defPreferences.getString("theme_key", "light") != currentTheme){
            recreate()
            Log.d("MyLog", "theme recreate")
        }
    }

    private fun getSelectedTheme(): Int{
        return if (defPreferences.getString("theme_key", "light") == "light"){
            R.style.Base_Theme_ShoppingListLight
        }else{
            R.style.Base_Theme_ShoppingListDark
        }
    }

    override fun onClick(name: String) {
        Log.d("MyLog", "Name: $name")

    }
}