package com.dicoding.myfirebasechat.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
  val text: String? = null,
  val name: String? = null,
  val photoUrl: String? = null,
  val timestamp: Long? = null
) {

}