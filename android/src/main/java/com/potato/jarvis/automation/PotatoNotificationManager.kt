package com.potato.jarvis.automation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.potato.jarvis.MainActivity
import com.potato.jarvis.screenForPotatoDeepLink
import com.potato.jarvis.core.NotificationItem

class PotatoNotificationManager(private val context: Context) {
    companion object {
        const val CHANNEL_ASSISTANT = "potato_assistant"
        const val CHANNEL_APPROVALS = "potato_approvals"
        const val CHANNEL_TASKS = "potato_tasks"
        const val CHANNEL_SYSTEM = "potato_system"
    }

    fun canPost(): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(item: NotificationItem) {
        if (!canPost()) return
        val channel = when (item.type) {
            "approval" -> CHANNEL_APPROVALS
            "task", "automation" -> CHANNEL_TASKS
            "system", "failure" -> CHANNEL_SYSTEM
            else -> CHANNEL_ASSISTANT
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (item.priority == "urgent" || item.priority == "high") NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            manager.createNotificationChannel(NotificationChannel(channel, channelTitle(channel), importance))
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            item.deepLink?.takeIf { screenForPotatoDeepLink(it) != null }?.let { data = android.net.Uri.parse(it) }
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(context, item.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(com.potato.jarvis.R.drawable.ic_stat_potato)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(if (item.priority == "urgent" || item.priority == "high") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(item.id.hashCode(), notification)
    }

    private fun channelTitle(channel: String): String = when (channel) {
        CHANNEL_APPROVALS -> "POTATO Approvals"
        CHANNEL_TASKS -> "POTATO Tasks"
        CHANNEL_SYSTEM -> "POTATO System"
        else -> "POTATO Assistant"
    }
}
