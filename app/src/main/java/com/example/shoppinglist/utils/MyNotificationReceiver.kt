package com.example.shoppinglist.utils

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.shoppinglist.R
import com.example.shoppinglist.activities.MainActivity
import com.example.shoppinglist.activities.MainApp
import com.example.shoppinglist.activities.ShopListActivity
import com.example.shoppinglist.activities.ShopListActivity.Companion.SHOP_LIST_NAME_ITEM
import com.example.shoppinglist.db.MainViewModel
import com.example.shoppinglist.entities.ShopListNameItem
import com.google.gson.Gson

class MyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null) {
            // Получите переданные данные списка
            val shopListNameItem = intent.getSerializableExtra("SHOP_LIST_NAME_ITEM") as ShopListNameItem
            Log.d("MyLog", "id list ittem3 - $shopListNameItem")

            //Log.d("MyLog", "id list ittem - ${shopListNameItem?.id}")

            // Создайте интент для открытия активности и передайте данные списка
//            val activityIntent = Intent(context, ShopListActivity::class.java)
//            activityIntent.putExtra(SHOP_LIST_NAME_ITEM, shopListNameItem)
            val preferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = preferences?.edit()
            editor?.putString("shopListNameItem", Gson().toJson(shopListNameItem))
            editor?.apply()


            // Получите переданные данные (год, месяц, день, час, минуту) из Intent
            val year = intent.getIntExtra("year", 0)
            Log.d("MyLog", "year - $year")
            val month = intent.getIntExtra("month", 0)
            val day = intent.getIntExtra("day", 0)
            val hour = intent.getIntExtra("hour", 0)
            val minute = intent.getIntExtra("minute", 0)

            // Создайте канал уведомлений (для Android 8.0 и выше)
            createNotificationChannel(context)

            val title = intent.getStringExtra("TITLE")

            // Создайте уведомление
            val builder = NotificationCompat.Builder(context!!, "your_channel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("It's time to shop! \uD83D\uDED2")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntent(context, year, month, day, hour, minute))
                .setAutoCancel(true)

            // Отправьте уведомление
            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Обработайте ситуацию, когда разрешение на отправку уведомлений не предоставлено
                    // Можно запросить разрешение или предоставить какую-либо обратную связь пользователю
                    return
                }
                notify(1, builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "your_channel_id",
                "Your Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Your channel description"
            }

            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createPendingIntent(
        context: Context?,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): PendingIntent? {
        val intent = Intent(context, ShopListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("year", year)
        intent.putExtra("month", month)
        intent.putExtra("day", day)
        intent.putExtra("hour", hour)
        intent.putExtra("minute", minute)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}