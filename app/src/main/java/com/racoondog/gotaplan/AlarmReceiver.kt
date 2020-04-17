package com.racoondog.gotaplan

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import io.realm.Realm
import io.realm.RealmResults
import java.util.*


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val realm = Realm.getDefaultInstance()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)
        val id = intent.getIntExtra("id",0)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingI = PendingIntent.getActivity(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, "0")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val soundUri =
            Uri.parse("android.resource://" + context.packageName.toString() + "/" + R.raw.note)
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "myapp:mywakelocktag"
        )

        wakeLock.acquire(3000)

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_app) //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남

            val channelName = "매일 알람 채널"
            val description = "매일 정해진 시간에 알람합니다."
            val importance = NotificationManager.IMPORTANCE_HIGH //소리와 알림메시지를 같이 보여줌
            val channel =
                NotificationChannel("0", channelName, importance)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            channel.description = description
            channel.setSound(soundUri,audioAttributes)
            notificationManager?.createNotificationChannel(channel)

        } else builder.setSmallIcon(R.mipmap.ic_launcher) // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        val subjectData: RealmResults<SubjectData> = realm.where<SubjectData>(SubjectData::class.java)
            .equalTo("id",id)
            .findAll()
        val data = subjectData[0]!!

        if(data.lessonOnOff && data.maxCycle != 0 ){

            val applyIntent = Intent(context, LessonNotification::class.java).putExtra("id",id)
            applyIntent.action = "apply"
            val pi1 = PendingIntent.getBroadcast(context, id, applyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val cancelIntent = Intent(context, LessonNotification::class.java).putExtra("id",id)
            cancelIntent.action = "cancel"
            val pi2 = PendingIntent.getBroadcast(context, id, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val contentView = RemoteViews(context.packageName,R.layout.lesson_notification)
            contentView.setTextViewText(R.id.notification_title,"레슨 알림")
            contentView.setTextViewText(R.id.notification_content,data.title)
            contentView.setOnClickPendingIntent(R.id.lesson_notification_apply,pi1)
            contentView.setOnClickPendingIntent(R.id.lesson_notification_cancel,pi2)

            builder.setAutoCancel(false)

                //.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(contentView)
                .setOngoing(true)
                //.setContentInfo("INFO")
                .setContentIntent(pendingI)
                .setSound(soundUri)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        } else{

            builder.setAutoCancel(true)

                //.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("일정 알림")
                .setContentText(data.title)
                //.setOngoing(true)
                //.setContentInfo("INFO")
                .setContentIntent(pendingI)
                .setSound(soundUri)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        }


        // 노티피케이션 동작시킴

        notificationManager.notify(id, builder.build())

        val nextNotifyTime = Calendar.getInstance()

        // 내일 같은 시간으로 알람시간 결정
        nextNotifyTime.add(Calendar.DATE, 7)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nextNotifyTime.timeInMillis,pendingIntent)
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,nextNotifyTime.timeInMillis,pendingIntent)
        }
        //  Preference에 설정한 값 저장
        val editor =
            context.getSharedPreferences("alarm", MODE_PRIVATE)
                .edit()
        editor.putLong("$id", nextNotifyTime.timeInMillis)
        editor.apply()


    }
}