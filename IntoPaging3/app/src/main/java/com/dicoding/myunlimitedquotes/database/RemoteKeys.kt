package com.dicoding.myunlimitedquotes.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// tabel yang menampung data halaman terbaru dari API
@Entity(tableName = "remote_keys")
data class RemoteKeys(
  @PrimaryKey val id: String,
  val prevKey: Int?,
  val nextKey: Int?,
)
