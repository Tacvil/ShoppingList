package com.example.shoppinglist.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityNewNoteBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.entities.NoteItem
import com.example.shoppinglist.fragments.NoteFragment
import com.example.shoppinglist.utils.HtmlManager
import com.example.shoppinglist.utils.MyTouchListener
import com.example.shoppinglist.utils.TimeManager.getCurrentTime

class NewNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewNoteBinding
    private var note: NoteItem? = null
    private var pref: SharedPreferences? = null
    private lateinit var defPreferences: SharedPreferences
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((applicationContext as MainApp).database, application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        init()
        setTextSize()
        getNote()
        onClickColorPicker()
        //actionMenuCallback()


    }

    private fun onClickColorPicker() = with(binding) {
        imageButtonRed.setOnClickListener {
            setColorForSelectedText(R.color.picker_red)
        }
        imageButtonBlack.setOnClickListener {
            setColorForSelectedText(R.color.picker_black)
        }
        imageButtonBlue.setOnClickListener {
            setColorForSelectedText(R.color.picker_blue)
        }
        imageButtonYellow.setOnClickListener {
            setColorForSelectedText(R.color.picker_yellow)
        }
        imageButtonGreen.setOnClickListener {
            setColorForSelectedText(R.color.picker_green)
        }
        imageButtonOrange.setOnClickListener {
            setColorForSelectedText(R.color.picker_orange)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        binding.colorPicker.setOnTouchListener(MyTouchListener())
        pref = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun getNote() {
        val sNote = intent.getSerializableExtra(NoteFragment.NEW_NOTE_KEY)
        if (sNote != null) {
            note = sNote as NoteItem
            fillNote()
        }
    }

    private fun fillNote() = with(binding) {
        editTextTitle.setText(note?.title)
        editTextDescription.setText(HtmlManager.getFromHtml(note?.context!!).trim())

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_save) {
            setMainResult()
        } else if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.id_bold) {
            setBoldForSelectedText()
        } else if (item.itemId == R.id.id_color) {
            if (binding.colorPicker.isShown) {
                closeColorPicker()
            } else {
                openColorPicker()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBoldForSelectedText() = with(binding) {
        val startPos = editTextDescription.selectionStart
        val endPos = editTextDescription.selectionEnd

        val styles = editTextDescription.text?.getSpans(startPos, endPos, StyleSpan::class.java)
        var boldStyle: StyleSpan? = null

        if (styles != null) {
            if (styles.isNotEmpty()) {
                editTextDescription.text?.removeSpan(styles[0])
            } else {
                boldStyle = StyleSpan(Typeface.BOLD)
            }
        }
        editTextDescription.text?.setSpan(
            boldStyle,
            startPos,
            endPos,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        editTextDescription.text?.trim()
        editTextDescription.setSelection(startPos)
    }

    private fun setColorForSelectedText(colorId: Int) = with(binding) {
        val startPos = editTextDescription.selectionStart
        val endPos = editTextDescription.selectionEnd

        val styles =
            editTextDescription.text?.getSpans(startPos, endPos, ForegroundColorSpan::class.java)

        if (styles!!.isNotEmpty()) editTextDescription.text?.removeSpan(styles[0])

        editTextDescription.text?.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@NewNoteActivity,
                    colorId
                )
            ), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        editTextDescription.text?.trim()
        editTextDescription.setSelection(startPos)
    }

    private fun setMainResult() {
        var editState = "new"
        var tempNote: NoteItem? = null
        if (note == null) {
            if (binding.editTextTitle.text?.isNotEmpty() == true || binding.editTextDescription.text?.isNotEmpty() == true) {
                tempNote = createNewNote()
            } else {
                finish()
            }
        } else {
            if(binding.editTextTitle.text?.isNotEmpty() == true || binding.editTextDescription.text?.isNotEmpty() == true){
                editState = "update"
                tempNote = updateNote()
            } else{
                note!!.id?.let { mainViewModel.deleteNote(it) }
            }
//            editState = "update"
//            tempNote = updateNote()
        }
        val i = Intent().apply {
            putExtra(NoteFragment.NEW_NOTE_KEY, tempNote)
            putExtra(NoteFragment.EDIT_STATE_KEY, editState)
        }
        setResult(RESULT_OK, i)
        finish()
    }

    private fun updateNote(): NoteItem? = with(binding) {
        return note?.copy(
            title = editTextTitle.text.toString(),
            context = HtmlManager.toHtml(editTextDescription.text!!)
        )
    }

    private fun createNewNote(): NoteItem {
        return NoteItem(
            null,
            binding.editTextTitle.text.toString(),
            HtmlManager.toHtml(binding.editTextDescription.text!!),
            getCurrentTime(),
            ""
        )
    }

    private fun actionBarSettings() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun openColorPicker() {
        binding.colorPicker.visibility = View.VISIBLE
        val openAnim = AnimationUtils.loadAnimation(this, R.anim.open_color_pickre)
        binding.colorPicker.startAnimation(openAnim)
    }

    private fun closeColorPicker() {
        val closeAnim = AnimationUtils.loadAnimation(this, R.anim.close_color_pickre)
        closeAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.colorPicker.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
        binding.colorPicker.startAnimation(closeAnim)
    }

    private fun actionMenuCallback() {
        val actionCallback = object : ActionMode.Callback2() {

            override fun onCreateActionMode(p0: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, menu: Menu?): Boolean {
                menu?.clear()
                return true
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {

            }

        }
        binding.editTextDescription.customSelectionActionModeCallback = actionCallback
    }

    private fun setTextSize() = with(binding) {
        val titleTextSize = pref?.getInt("title_size_key", 16)
        titleTextSize?.toFloat()?.let { editTextTitle.textSize = it }

        val contextTextSize = pref?.getInt("content_size_key", 14)
        contextTextSize?.toFloat()?.let { editTextDescription.textSize = it }

    }

    private fun getSelectedTheme(): Int {
        return if (defPreferences.getString("theme_key", "light") == "light") {
            R.style.Base_Theme_ShoppingListLight
        } else {
            R.style.Base_Theme_ShoppingListDark
        }
    }

}