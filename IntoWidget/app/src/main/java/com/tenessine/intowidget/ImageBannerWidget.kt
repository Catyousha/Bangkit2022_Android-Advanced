package com.tenessine.intowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Implementation of App Widget functionality.
 */
class ImageBannerWidget : AppWidgetProvider() {
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

  // Called when the user taps the widget
  override fun onReceive(context: Context?, intent: Intent?) {
    super.onReceive(context, intent)
    if(intent?.action == TOAST_ACTION) {
      val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
      Toast.makeText(context, "Touched view $viewIndex", Toast.LENGTH_SHORT).show()
    }
  }

  companion object {
    private const val TOAST_ACTION = "com.tenessine.intowidget.TOAST_ACTION"
    const val EXTRA_ITEM = "com.tenessine.intowidget.EXTRA_ITEM"

    private fun updateAppWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
    ) {

      val intent = Intent(context, StackWidgetService::class.java).apply {
        // set widget id
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        // data diambil dari StackWidgetService
        data = this.toUri(Intent.URI_INTENT_SCHEME).toUri()
      }

      // memasang RemoteViews yang berisi adapter versi remote
      val views = RemoteViews(context.packageName, R.layout.image_banner_widget).apply {
        // adapter tersebut direpresentasikan dalam R.id.stack_view dengan data di intent
        setRemoteAdapter(R.id.stack_view, intent)

        // kalau data kosong, maka akan ditampilkan R.id.empty_view
        setEmptyView(R.id.stack_view, R.id.empty_view)
      }

      // membuat intent aksi
      val toastIntent = Intent(context, ImageBannerWidget::class.java).apply {
        action = TOAST_ACTION
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      }

      val toastPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
      } else {
        0
      }

      // membuat pending intent untuk mengirimkan data ke onReceive
      val toastPendingIntent =
        PendingIntent.getBroadcast(context, 0, toastIntent, toastPendingIntentFlags)

      // memasang intent aksi ke widget
      views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
