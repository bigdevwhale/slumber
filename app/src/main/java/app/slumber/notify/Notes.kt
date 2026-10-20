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
import app.slumber.data.Check
import app.slumber.data.Settings
import app.slumber.data.morningOf
import app.slumber.domain.Checks
import app.slumber.domain.Plan
import app.slumber.localized
import app.slumber.ru
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * The three quiet notes of the day: "What did you dream?" at waking, a few reality checks at moments
 * nobody can guess, and the ritual before bed. Each is skipped when it's already been done.
 */
object Notes {
    private const val CH_MORNING = "morning_v1"
    private const val CH_CHECKS = "checks_v1"
    private const val CH_BEDTIME = "bedtime_v1"

    internal const val MORNING = "app.slumber.notify.MORNING"
    internal const val CHECK = "app.slumber.notify.CHECK"
    internal const val BEDTIME = "app.slumber.notify.BEDTIME"
    internal const val CHECKED = "app.slumber.notify.CHECKED"

    private const val ID_MORNING = 5
    private const val ID_CHECK = 6
    private const val ID_BEDTIME = 7

    /** Alarms come within this window: a dream fades in minutes, and a check should feel sudden. */
    private const val WINDOW_MS = 10 * 60_000L

    /** The ritual note comes this long before bedtime. */
    const val BEFORE_BED_MIN = 30

    /** A check just done: the next nudge within this long is skipped. */
    private const val RECENT_MS = 30 * 60_000L

    /** One short, soft buzz: a tap on the shoulder. */
    private val buzz = longArrayOf(0, 60)

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val l = context.localized()
        fun channel(id: String, name: Int, desc: Int, importance: Int, vibrate: Boolean) {
            if (nm.getNotificationChannel(id) != null) return
            nm.createNotificationChannel(NotificationChannel(id, l.getString(name), importance).apply {
                description = l.getString(desc)
                setSound(null, null)
                enableVibration(vibrate)
                if (vibrate) vibrationPattern = buzz
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
        }
        channel(CH_MORNING, R.string.ch_morning, R.string.ch_morning_desc, NotificationManager.IMPORTANCE_DEFAULT, false)
        channel(CH_CHECKS, R.string.ch_checks, R.string.ch_checks_desc, NotificationManager.IMPORTANCE_HIGH, true)
        channel(CH_BEDTIME, R.string.ch_bedtime, R.string.ch_bedtime_desc, NotificationManager.IMPORTANCE_DEFAULT, false)
    }

    private fun pi(context: Context, action: String, code: Int) = PendingIntent.getBroadcast(
        context, code, Intent(context, NoteReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun daily(minute: Int, now: LocalDateTime): LocalDateTime {
        val at = LocalDateTime.of(now.toLocalDate(), LocalTime.of(minute / 60, minute % 60))
        return if (at.isAfter(now)) at else at.plusDays(1)
    }

    fun nextMorning(s: Settings, now: LocalDateTime): LocalDateTime? = if (s.onboarded && s.morning) daily(s.wake, now) else null
    fun nextBedtime(s: Settings, now: LocalDateTime): LocalDateTime? =
        if (s.onboarded && s.bedtimeNote) daily(((s.bedtime - BEFORE_BED_MIN) % 1440 + 1440) % 1440, now) else null
    fun nextCheck(s: Settings, now: LocalDateTime): LocalDateTime? =
        if (s.onboarded && s.checks) Plan.next(now, s.from, s.to, s.perDay, s.salt) else null

    suspend fun reschedule(context: Context) {
        val s = context.container.settings.now()
        val am = context.getSystemService(AlarmManager::class.java)
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        for ((action, code, at) in listOf(
            Triple(MORNING, 1, nextMorning(s, now)),
            Triple(CHECK, 2, nextCheck(s, now)),
            Triple(BEDTIME, 3, nextBedtime(s, now)),
        )) {
            val p = pi(context, action, code)
            if (at == null) am.cancel(p)
            // A window rather than an exact time: no exact-alarm permission, and still within minutes.
            else am.setWindow(AlarmManager.RTC_WAKEUP, at.atZone(zone).toInstant().toEpochMilli(), WINDOW_MS, p)
        }
    }

    suspend fun fire(context: Context, action: String) {
        val c = context.container
        val s = c.settings.now()
        if (!s.onboarded || !canNotify(context)) return
        val today = morningOf(System.currentTimeMillis())
        when (action) {
            MORNING -> if (s.morning && c.db.dao().allDreams().none { it.day() == today }) morning(context)
            BEDTIME -> if (s.bedtimeNote && c.db.dao().allRituals().none { morningOf(it.at) == today }) bedtime(context)
            CHECK -> {
                val last = c.db.dao().lastCheck()
                if (s.checks && (last == null || System.currentTimeMillis() - last.at > RECENT_MS)) check(context)
            }
        }
    }

    private fun open(context: Context, where: String, code: Int) = PendingIntent.getActivity(
        context, code,
        Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN, where)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun post(context: Context, id: Int, n: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) {
        }
    }

    fun morning(context: Context) {
        val l = context.localized()
        post(context, ID_MORNING, NotificationCompat.Builder(context, CH_MORNING)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.lamp))
            .setContentTitle(l.getString(R.string.morning_title))
            .setContentText(l.getString(R.string.morning_text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSilent(true)
            .setTimeoutAfter(60 * 60_000L)
            .setContentIntent(open(context, MainActivity.OPEN_CAPTURE, 100))
            .setAutoCancel(true)
            .build())
    }

    /** "Are you dreaming?" with one of the checks; "Checked" answers from the notification itself. */
    fun check(context: Context) {
        val l = context.localized()
        val prompt = Checks.pick(System.currentTimeMillis() / 60_000).text(l.ru)
        val checked = PendingIntent.getBroadcast(
            context, 102, Intent(context, NoteReceiver::class.java).setAction(CHECKED),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        post(context, ID_CHECK, NotificationCompat.Builder(context, CH_CHECKS)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.lamp))
            .setContentTitle(l.getString(R.string.check_title))
            .setContentText(prompt)
            .setStyle(NotificationCompat.BigTextStyle().bigText(prompt))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(buzz)
            .setTimeoutAfter(15 * 60_000L)
            .setContentIntent(open(context, MainActivity.OPEN_CHECK, 101))
            .addAction(0, l.getString(R.string.check_done), checked)
            .setAutoCancel(true)
            .build())
    }

    fun bedtime(context: Context) {
        val l = context.localized()
        post(context, ID_BEDTIME, NotificationCompat.Builder(context, CH_BEDTIME)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.lamp))
            .setContentTitle(l.getString(R.string.bedtime_title))
            .setContentText(l.getString(R.string.bedtime_text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSilent(true)
            .setTimeoutAfter(3 * 60 * 60_000L)
            .setContentIntent(open(context, MainActivity.OPEN_RITUAL, 103))
            .setAutoCancel(true)
            .build())
    }

    fun clear(context: Context) = NotificationManagerCompat.from(context).let {
        it.cancel(ID_MORNING)
        it.cancel(ID_CHECK)
        it.cancel(ID_BEDTIME)
    }

    fun clearCheck(context: Context) = NotificationManagerCompat.from(context).cancel(ID_CHECK)
}

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class NoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(Notes.MORNING, Notes.CHECK, Notes.BEDTIME, Notes.CHECKED)) return
        if (action == Notes.CHECKED) Notes.clearCheck(context)
        val pending = goAsync()
        receiverScope.launch {
            try {
                if (action == Notes.CHECKED) context.container.db.dao().insert(Check())
                else {
                    Notes.fire(context, action)
                    Notes.reschedule(context)
                }
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
                Notes.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
