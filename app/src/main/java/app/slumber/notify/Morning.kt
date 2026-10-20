package app.slumber.notify

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.slumber.MainActivity
import app.slumber.R
import app.slumber.container
import app.slumber.data.Settings
import app.slumber.localized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * One quiet note a day, at the time you usually wake: "What did you dream?". A dream fades in minutes,
 * so the note goes away after an hour, and doesn't come at all once a dream is told that morning.
 */
object Morning {
    private const val CHANNEL = "morning_v1"
    private const val ID = 5
    internal const val FIRE = "app.slumber.notify.MORNING"
    private const val TIMEOUT_MS = 60 * 60_000L
    private const val WINDOW_MS = 10 * 60_000L

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) != null) return
        val l = context.localized()
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, l.getString(R.string.channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = l.getString(R.string.channel_desc)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }

    private fun pi(context: Context) = PendingIntent.getBroadcast(
        context, 1,
        Intent(context, MorningReceiver::class.java).setAction(FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** The next waking time, or null with the note off. */
    fun next(s: Settings, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (!s.onboarded || !s.morning) return null
        val at = LocalDateTime.of(now.toLocalDate(), LocalTime.of(s.wake / 60, s.wake % 60))
        return if (at.isAfter(now)) at else at.plusDays(1)
    }

    suspend fun reschedule(context: Context) {
        val s = context.container.settings.now()
        val am = context.getSystemService(AlarmManager::class.java)
        val p = pi(context)
        val zone = ZoneId.systemDefault()
        val at = next(s, LocalDateTime.now(zone))
        if (at == null) {
            am.cancel(p)
            return
        }
        // A dream fades in minutes: setAndAllowWhileIdle may come an hour late, so ask for a ten-minute window
        // (no exact-alarm permission needed). At waking the phone is picked up, which ends Doze anyway.
        am.setWindow(AlarmManager.RTC_WAKEUP, at.atZone(zone).toInstant().toEpochMilli(), WINDOW_MS, p)
    }

    suspend fun fire(context: Context) {
        val s = context.container.settings.now()
        if (!s.onboarded || !s.morning || !canNotify(context)) return
        val today = LocalDate.now()
        if (context.container.db.dao().allDreams().any { it.day() == today }) return
        show(context)
    }

    fun show(context: Context) {
        val l = context.localized()
        val open = PendingIntent.getActivity(
            context, 100,
            Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN, MainActivity.OPEN_CAPTURE)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.red))
            .setContentTitle(l.getString(R.string.morning_title))
            .setContentText(l.getString(R.string.morning_text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setTimeoutAfter(TIMEOUT_MS)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(ID, n)
        } catch (_: SecurityException) {
        }
    }

    fun clear(context: Context) = NotificationManagerCompat.from(context).cancel(ID)
}

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class MorningReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Morning.FIRE) return
        val pending = goAsync()
        receiverScope.launch {
            try {
                Morning.fire(context)
                Morning.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    private val actions = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in actions) return
        val pending = goAsync()
        receiverScope.launch {
            try {
                Morning.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
