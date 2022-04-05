package com.tenessine.intowidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf

// kayak adapter untuk recyclerview, cuman versi banner widget
internal class StackRemoteViewsFactory(private val mContext: Context) :
  RemoteViewsService.RemoteViewsFactory {

  private val mWidgetItems = ArrayList<Bitmap>()

  override fun onCreate() {
  }

  // inisialisasi data apa saja yang bakal ditampilin di widget
  override fun onDataSetChanged() {
    mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.darth_vader))
    mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.star_wars_logo))
    mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.storm_trooper))
    mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.starwars))
    mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.falcon))
  }

  override fun onDestroy() {
  }

  // jumlah item yang bakal ditampilin
  override fun getCount(): Int {
    return mWidgetItems.size
  }

  override fun getViewAt(position: Int): RemoteViews {
    // data yang bakal dikirim
    // berupa angka indeks
    val extras = bundleOf(
      ImageBannerWidget.EXTRA_ITEM to position
    )

    // bungkus data yang bakal dikirim
    val fillInIntent = Intent().apply {
      putExtras(extras)
    }

    // buat gambar ditampilkan dalam R.layout.image_banner_item
    val rv = RemoteViews(mContext.packageName, R.layout.image_banner_item).apply {
      // kemudian set imageviewnya sesuai dengan data yang diperoleh dari onDataSetChanged
      setImageViewBitmap(R.id.imageView, mWidgetItems[position])

      // pas item ditampilkan penuh dan diklik, maka data extras akan dikirim ke ImageBannerWidget
      setOnClickFillInIntent(R.id.imageView, fillInIntent)
    }

    return rv
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  // jenis layout yang bakal ditampilin
  override fun getViewTypeCount(): Int {
    return 1
  }

  override fun getItemId(position: Int): Long {
    return 0
  }

  override fun hasStableIds(): Boolean {
    return false
  }

}