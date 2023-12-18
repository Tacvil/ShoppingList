package com.example.shoppinglist.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppinglist.R
import com.example.shoppinglist.databinding.ActivityShopListBinding
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.db.ShopListItemAdapter
import com.example.shoppinglist.dialogs.EditListItemDialog
import com.example.shoppinglist.entities.LibraryItem
import com.example.shoppinglist.entities.ShopListItem
import com.example.shoppinglist.entities.ShopListNameItem
import com.example.shoppinglist.utils.MyNotificationReceiver
import com.example.shoppinglist.utils.ShareHelper
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ShopListActivity : AppCompatActivity(), ShopListItemAdapter.Listener {
    private lateinit var binding: ActivityShopListBinding
    private var shopListNameItem: ShopListNameItem? = null
    private lateinit var saveItem: MenuItem
    private var edItem: EditText? = null
    private var adapter: ShopListItemAdapter? = null
    private lateinit var textWatcher: TextWatcher
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory((applicationContext as MainApp).database, application)
    }
    private lateinit var defPreferences: SharedPreferences

    private lateinit var alarmManager: AlarmManager
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

    private lateinit var selectDateTimeButton: Button

    private var setDateTime: Long = 0

    private var counter : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme())
        binding = ActivityShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        if (shopListNameItem == null){
            shopListNameItem = checkPrefab()
            counter = true
        }

        initRcView()
        listItemObserver()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (counter){
                    saveItemCount()
                    navigateToMainActivity()
                }

                saveItemCount()
                isEnabled = true
                finish()
            }
        }

        if (shopListNameItem?.isStarted == true) {
            updateDateTime()
        }

        onBackPressedDispatcher.addCallback(this, callback)

        //начало нотифик
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        selectDateTimeButton = findViewById(R.id.selectDateTimeButton)


        selectDateTimeButton.setOnClickListener {
            showDateTimePickerDialog()
        }

        binding.iClearReminder.apply {
            setOnClickListener {
                binding.selectDateTimeButton.setText(R.string.tv_set_reminder)
                visibility = View.INVISIBLE
                setDateTime = 0
                shopListNameItem?.isStarted = false
//                val tempShopListItem = shopListNameItem?.copy(
//                    isStarted = false
//                )
//                mainViewModel.updateListName(tempShopListItem!!)
                cancelAlarm(this@ShopListActivity)
            }
        }

    }

    private fun checkPrefab(): ShopListNameItem? {
        val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val shopListNameItemJson = preferences.getString("shopListNameItem", null)
        Log.d("MyLog", "shopListNameItemJson Create - $shopListNameItemJson")
        return if (shopListNameItemJson == null){
            null
        }else{
            Gson().fromJson(shopListNameItemJson, ShopListNameItem::class.java)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        saveItemCount()
        finish()
    }

    fun clearRemaind(){
        binding.selectDateTimeButton.setText(R.string.tv_set_reminder)
        binding.iClearReminder.visibility = View.INVISIBLE
        setDateTime = 0
        //shopListNameItem?.isStarted = false
    }

    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        // Вызов метода для настройки уведомления
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(Calendar.YEAR, year)
                        selectedDate.set(Calendar.MONTH, month)
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedDate.set(Calendar.MINUTE, minute)

                        val setDateTimeForTextView = selectedDate.timeInMillis
                        setDateTime = selectedDate.timeInMillis
                        //shopListNameItem.reminderData = Date(setDateTimeForTextView)

                        shopListNameItem?.reminderData = Date(setDateTimeForTextView)
                        shopListNameItem?.isStarted = true
                        Log.d("MyLog", "dataRefr ${shopListNameItem?.isStarted}")
//                        val tempShopListItem = shopListNameItem?.copy(
//                            reminderData = Date(setDateTimeForTextView),
//                            isStarted = true
//                        )
//                        mainViewModel.updateListName(tempShopListItem!!)

                        updateDateTime()

                        schedule(this, selectedDate)

                    },
                    currentHour,
                    currentMinute,
                    true // Использовать 24-часовой формат
                )
                timePickerDialog.show()
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(context: Context, selectedDate: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyNotificationReceiver::class.java)
        intent.putExtra("TITLE", shopListNameItem?.name)
        intent.putExtra(SHOP_LIST_NAME_ITEM, shopListNameItem)
        val alarmPendingIntent =
            shopListNameItem?.id?.let {
                PendingIntent.getBroadcast(
                    context, it, intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        if (alarmPendingIntent != null) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                selectedDate.timeInMillis,
                alarmPendingIntent
            )
        }
        shopListNameItem?.isStarted = true
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyNotificationReceiver::class.java)
        val alarmPendingIntent =
            shopListNameItem?.id?.let { PendingIntent.getBroadcast(context, it, intent,
                PendingIntent.FLAG_IMMUTABLE) }
        if (alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent)
        }
        shopListNameItem?.isStarted = false
    }

    private fun updateDateTime() {
        binding.iClearReminder.visibility = View.VISIBLE

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = shopListNameItem?.reminderData?.let { Date(it.time) }
            ?.let { timeFormat.format(it) }

        val relativeDate = shopListNameItem?.reminderData?.let {
            DateUtils.getRelativeDateTimeString(
                this,
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
        binding.selectDateTimeButton.text = dateTimeString
        Log.d("MyLog", "dataRefr ${shopListNameItem?.reminderData?.time}")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.shop_list_menu, menu)
        saveItem = menu?.findItem(R.id.save_item)!!
        val newItem = menu.findItem(R.id.new_item)!!
        edItem = newItem.actionView?.findViewById(R.id.edNewShopItem) as EditText
        newItem.setOnActionExpandListener(expandActionView())
        saveItem.isVisible = false
        textWatcher = textWatcher()
        return true
    }

    private fun textWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mainViewModel.getAllLibraryItems("%$s%")
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_item -> {
                addNewShopItem(edItem?.text.toString())
            }

            R.id.delete_list -> {
                mainViewModel.deleteShopList(shopListNameItem?.id!!, true)
                finish()
            }

            R.id.clear_list -> {
                mainViewModel.deleteShopList(shopListNameItem?.id!!, false)
            }

            R.id.share_list -> {
                startActivity(
                    Intent.createChooser(
                        ShareHelper.shareShopList(adapter?.currentList!!, shopListNameItem?.name!!),
                        "Share by"
                    )
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addNewShopItem(name: String) {
        if (name.isEmpty()) return
        val item = ShopListItem(
            null,
            name,
            null,
            false,
            shopListNameItem?.id!!,
            0
        )
        edItem?.text?.clear()
        mainViewModel.insertShopItem(item)
    }

    private fun listItemObserver() {
        mainViewModel.getAllItemsFromList(shopListNameItem?.id!!).observe(
            this
        ) {
            adapter?.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun libraryItemObserver() {
        mainViewModel.libraryItems.observe(this) {
            val tempShopList = ArrayList<ShopListItem>()
            it.forEach { item ->
                val shopItem = ShopListItem(
                    item.id,
                    item.name,
                    "",
                    false,
                    0,
                    1
                )
                tempShopList.add(shopItem)
            }
            adapter?.submitList(tempShopList)
            binding.tvEmpty.visibility = if (it.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun initRcView() = with(binding) {
        adapter = ShopListItemAdapter(this@ShopListActivity)
        rcView.layoutManager = LinearLayoutManager(this@ShopListActivity)
        rcView.adapter = adapter
    }

    private fun expandActionView(): MenuItem.OnActionExpandListener {
        return object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                saveItem.isVisible = true
                edItem?.addTextChangedListener(textWatcher)
                libraryItemObserver()
                mainViewModel.getAllItemsFromList(shopListNameItem?.id!!)
                    .removeObservers(this@ShopListActivity)
                mainViewModel.getAllLibraryItems("%%")
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                saveItem.isVisible = false
                edItem?.removeTextChangedListener(textWatcher)
                invalidateOptionsMenu()
                mainViewModel.libraryItems.removeObservers(this@ShopListActivity)
                edItem?.text?.clear()
                listItemObserver()
                return true
            }
        }
    }

    private fun init() {
        if (intent.getSerializableExtra(SHOP_LIST_NAME) != null){
            shopListNameItem = intent.getSerializableExtra(SHOP_LIST_NAME) as ShopListNameItem
        }else{
            Log.d("MyLog", "init Create null")
        }
    }

    companion object {
        const val SHOP_LIST_NAME = "shop_list_name"
        const val SHOP_LIST_NAME_ITEM = "SHOP_LIST_NAME_ITEM"
        private const val CHANNEL_ID = "my_channel"
    }

    override fun onClickItem(shopListItem: ShopListItem, state: Int) {
        when (state) {
            ShopListItemAdapter.CHECK_BOX -> mainViewModel.updateListItem(shopListItem)
            ShopListItemAdapter.EDIT -> editListItem(shopListItem)
            ShopListItemAdapter.EDIT_LIBRARY_ITEM -> editLibraryItem(shopListItem)
            ShopListItemAdapter.ADD -> addNewShopItem(shopListItem.name)
            ShopListItemAdapter.DELETE_LIBRARY_ITEM -> {
                mainViewModel.deleteLibraryItem(shopListItem.id!!)
                mainViewModel.getAllLibraryItems("%${edItem?.text.toString()}%")
            }
        }
    }

    private fun editListItem(item: ShopListItem) {
        EditListItemDialog.showDialog(this, item, object : EditListItemDialog.Listener {
            override fun onClick(item: ShopListItem) {
                mainViewModel.updateListItem(item)
            }
        })
    }

    private fun editLibraryItem(item: ShopListItem) {
        EditListItemDialog.showDialog(this, item, object : EditListItemDialog.Listener {
            override fun onClick(item: ShopListItem) {
                mainViewModel.updateLibraryItem(LibraryItem(item.id, item.name))
                mainViewModel.getAllLibraryItems("%${edItem?.text.toString()}%")
            }
        })
    }

    private fun saveItemCount() {
        var checkedItemCounter = 0
        adapter?.currentList?.forEach {
            if (it.itemChecked) checkedItemCounter++
        }
        val tempShopListItem = shopListNameItem?.copy(
            allItemCounter = adapter?.itemCount!!,
            checkedItemCounter = checkedItemCounter,
        )
        mainViewModel.updateListName(tempShopListItem!!)
    }

//    override fun onBackPressed() {
//        saveItemCount()
//        val tempShopListItem = shopListNameItem?.copy(
//            isStarted = enableNotificationCheckBox.isChecked
//        )
//        mainViewModel.updateListName(tempShopListItem!!)
//        super.onBackPressed()
//    }

    private fun getSelectedTheme(): Int {
        return if (defPreferences.getString("theme_key", "light") == "light") {
            R.style.Base_Theme_ShoppingListLight
        } else {
            R.style.Base_Theme_ShoppingListDark
        }
    }

    override fun onPause() {
        super.onPause()
        saveItemCount()
    }

}