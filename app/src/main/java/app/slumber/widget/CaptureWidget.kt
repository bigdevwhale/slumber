package app.slumber.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.slumber.MainActivity
import app.slumber.R
import app.slumber.localized

/** "Tell a dream" on the home or lock screen: one tap, still half asleep, and you're writing. */
class CaptureWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val l = context.localized()
        val views = RemoteViews(context.packageName, R.layout.widget_capture)
        views.setTextViewText(R.id.title, l.getString(R.string.widget_title))
        views.setTextViewText(R.id.sub, l.getString(R.string.widget_sub))
        val open = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN, MainActivity.OPEN_CAPTURE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        views.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(context, 2, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        manager.updateAppWidget(ids, views)
    }
}
