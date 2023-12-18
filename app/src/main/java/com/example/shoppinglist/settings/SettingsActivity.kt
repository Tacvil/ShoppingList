package com.example.shoppinglist.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.shoppinglist.R
import com.example.shoppinglist.activities.MainActivity
import com.example.shoppinglist.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var defPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                startMainActivity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction().replace(R.id.placeHolder, SettingsFragment()).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            startMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_to_display", "fragment1")
        startActivity(intent)
    }

    private fun getSelectedTheme(): Int{
        return if (defPreferences.getString("theme_key", "light") == "light"){
            R.style.Base_Theme_ShoppingListLight
        }else{
            R.style.Base_Theme_ShoppingListDark
        }
    }

}