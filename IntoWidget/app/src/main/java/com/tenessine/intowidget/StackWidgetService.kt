package com.tenessine.intowidget

import android.content.Intent
import android.widget.RemoteViewsService

// cuman nyambungin ke factory doang
class StackWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
    return StackRemoteViewsFactory(this.applicationContext)
  }
}