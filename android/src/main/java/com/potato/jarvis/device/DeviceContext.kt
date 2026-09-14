package com.potato.jarvis.device

import android.Manifest
import android.content.ClipDescription
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.potato.jarvis.BuildConfig
import com.potato.jarvis.core.DeviceContextSnapshot
import com.potato.jarvis.accessibility.AccessibilityConsent
import com.potato.jarvis.accessibility.PotatoAccessibilityService
import java.text.DateFormat
import java.util.Date

class DeviceContext(private val context: Context) {
    fun snapshot(includeCalendar: Boolean, includeContacts: Boolean, includeLocation: Boolean): DeviceContextSnapshot {
        val battery = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else null
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val networkCapabilities = connectivity?.activeNetwork?.let { connectivity.getNetworkCapabilities(it) }
        val network = when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile data"
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            else -> "Offline"
        }

        val calendarGranted = includeCalendar && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val contactsGranted = includeContacts && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val locationGranted = includeLocation && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

        return DeviceContextSnapshot(
            batteryPercent = percent,
            charging = charging,
            network = network,
            online = networkCapabilities != null,
            device = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = BuildConfig.POTATO_VERSION,
            nextCalendarEvent = if (calendarGranted) nextCalendarEvent() else null,
            contactCount = if (contactsGranted) countContacts() else null,
            location = if (locationGranted) lastKnownLocation() else null,
            locationPermission = locationGranted,
            calendarPermission = calendarGranted,
            contactsPermission = contactsGranted,
            foregroundPackage = foregroundPackage(),
        )
    }

    private fun foregroundPackage(): String? {
        if (!AccessibilityConsent.isAccepted(context)) return null
        return context.getSharedPreferences(PotatoAccessibilityService.PREFS, Context.MODE_PRIVATE)
            .getString(PotatoAccessibilityService.KEY_FOREGROUND_PACKAGE, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(255)
    }

    fun readClipboard(): String? {
        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java) ?: return null
        if (!clipboard.hasPrimaryClip()) return null
        val description: ClipDescription? = clipboard.primaryClipDescription
        if (description != null && !description.hasMimeType("text/*")) return null
        val clip = clipboard.primaryClip ?: return null
        val text = clip.getItemAt(0).coerceToText(context).toString().trim()
        return text.takeIf { it.isNotBlank() }?.take(10_000)
    }

    private fun countContacts(): Int = runCatching {
        context.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, arrayOf(ContactsContract.Contacts._ID), null, null, null)?.use { cursor -> cursor.count } ?: 0
    }.getOrDefault(0)

    private fun nextCalendarEvent(): String? = runCatching {
        val now = System.currentTimeMillis()
        context.contentResolver.query(
            CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
                appendPath(now.toString())
                appendPath((now + 7L * 24 * 60 * 60 * 1000).toString())
            }.build(),
            arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_LOCATION),
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC",
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val title = cursor.getString(0)?.trim().orEmpty().ifBlank { "Untitled event" }
            val begin = cursor.getLong(1)
            val location = cursor.getString(2)?.trim().orEmpty()
            val whenText = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(begin))
            if (location.isBlank()) "$title • $whenText" else "$title • $whenText • $location"
        }
    }.getOrNull()

    private fun lastKnownLocation(): String? {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return null

        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        return try {
            val locations = manager.getProviders(true).mapNotNull { provider ->
                try {
                    manager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }
            val best = locations.maxByOrNull { it.time } ?: return null
            "%.5f, %.5f".format(java.util.Locale.US, best.latitude, best.longitude)
        } catch (_: SecurityException) {
            null
        }
    }
}
