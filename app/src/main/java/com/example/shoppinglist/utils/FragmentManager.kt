package com.example.shoppinglist.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shoppinglist.R
import com.example.shoppinglist.fragments.BaseFragment

object FragmentManager {
    var currentFrag: BaseFragment? = null

    fun Fragment.openFragment(f: Fragment) {
        (activity as AppCompatActivity).supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.place_holder, f).commit()
    }

    fun AppCompatActivity.openFragment(newFrag: BaseFragment, activity: AppCompatActivity) {
        //Log.d("MyLog","Frag name: ${f.javaClass}")
        if (supportFragmentManager.fragments.isNotEmpty()) {
            if (supportFragmentManager.fragments[0].javaClass == newFrag.javaClass) return
        }
        activity.supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.place_holder, newFrag).commit()

        currentFrag = newFrag
    }

    fun AppCompatActivity.openActivity(activityClass: Class<out Activity>) {
        val intent = Intent(this, activityClass)

        // Создайте объект ActivityOptions с анимациями
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )

        // Получите бандл из опций анимации и передайте его в startActivity
        val animationBundle: Bundle = options.toBundle()
        startActivity(intent, animationBundle)
    }

    fun Fragment.showToast(s: String) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    fun AppCompatActivity.showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
