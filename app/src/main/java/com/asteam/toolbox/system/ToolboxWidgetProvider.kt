package com.asteam.toolbox.system

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.asteam.toolbox.MainActivity
import com.asteam.toolbox.R
import com.asteam.toolbox.data.ToolCatalog
import com.asteam.toolbox.data.UserPreferences

/** Small launcher widget showing tool and favorite counts with one-tap app access. */
class ToolboxWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            val preferences = UserPreferences(context.applicationContext)
            val views = RemoteViews(context.packageName, R.layout.toolbox_widget)
            views.setTextViewText(R.id.widget_title, "جعبه ابزار")
            views.setTextViewText(
                R.id.widget_summary,
                "${ToolCatalog.tools.size} ابزار • ${preferences.favorites().size} علاقه‌مندی",
            )

            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
