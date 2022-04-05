package com.tenessine.intowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class RNGWidget : AppWidgetProvider() {

  // menerima perubahan widget tiap beberapa periode
  // yg ditentukan oleh r_n_g_widget_info.xml
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    // There may be multiple widgets active, so update all of them
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onEnabled(context: Context) {
    // Enter relevant functionality for when the first widget is created
  }

  override fun onDisabled(context: Context) {
    // Enter relevant functionality for when the last widget is disabled
  }

  // menerima segala event yang dibroadcast kedalam widget
  override fun onReceive(context: Context?, intent: Intent?) {
    super.onReceive(context, intent)
    // kalau eventnya berupa WIDGET_CLICK
    // ubah angka randomnya
    if (WIDGET_CLICK == intent?.action) {
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val views = RemoteViews(context?.packageName, R.layout.r_n_g_widget)
      val lastUpdate = "Random: " + NumberGenerator.geterate(42)

      // cari tau id widget mana yg berubah, bisa jadi ada lebih dari 1 widget
      val appWidgetId = intent.getIntExtra(WIDGET_ID_EXTRA, 0)
      views.setTextViewText(R.id.appwidget_text, lastUpdate)

      // eksekusi perubahan di widget sekian
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  // menerima event yang masuk dan dibroadcast ke seluruh widget
  private fun getPendingSelfIntent(
    context: Context,
    appWidgetId: Int,
    action: String
  ): PendingIntent {
    // membuat pending intent untuk dibroadcast
    val intent = Intent(context, RNGWidget::class.java)
    intent.apply {
      this.action = action
      putExtra(WIDGET_ID_EXTRA, appWidgetId)
    }

    // broadcast dapat mengupdate dan membuat widget mutable
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    } else {
      0
    }

    // broadcast action pada widget dengan id yg ditentukan (appWidgetId)
    return PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
  }

  // fungsi untuk mengupdate widget, dapat diakses melalui onUpdate (otomatis)
  // maupun onReceive (manual / perlu pemicu)
  private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
  ) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.r_n_g_widget)
    val lastUpdate = "Random: " + NumberGenerator.geterate(42)
    views.setTextViewText(R.id.appwidget_text, lastUpdate)

    // listener untuk event click pada widget
    views.setOnClickPendingIntent(
      R.id.btn_click,
      getPendingSelfIntent(context, appWidgetId, WIDGET_CLICK)
    )

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
  }

  companion object {
    private const val WIDGET_CLICK = "android.appwidget.action.APPWIDGET_UPDATE"
    private const val WIDGET_ID_EXTRA = "widget_id_extra"
  }
}
