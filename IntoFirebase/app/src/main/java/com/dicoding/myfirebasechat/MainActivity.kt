package com.dicoding.myfirebasechat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.myfirebasechat.adapter.FirebaseMessageAdapter
import com.dicoding.myfirebasechat.databinding.ActivityMainBinding
import com.dicoding.myfirebasechat.model.Message
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private lateinit var auth: FirebaseAuth
  private lateinit var db: FirebaseDatabase
  private lateinit var adapter: FirebaseMessageAdapter


  private var firebaseUser: FirebaseUser? = null
  private lateinit var messagesRef: DatabaseReference

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupAuth()
    setupDb()
    setupAdapter()

    binding.sendButton.setOnClickListener {
      sendChat()
    }
  }

  private fun setupAdapter() {
    val manager = LinearLayoutManager(this)
    manager.stackFromEnd = true
    binding.messageRecyclerView.layoutManager = manager

    // query data dalam database dan otomatis diubah ke model Message
    val options = FirebaseRecyclerOptions.Builder<Message>()
      .setQuery(messagesRef, Message::class.java)
      .build()

    adapter = FirebaseMessageAdapter(options, firebaseUser?.displayName)
    binding.messageRecyclerView.adapter = adapter
  }

  private fun setupDb() {
    db = Firebase.database
    messagesRef = db.reference.child(MESSAGES_CHILD)
  }

  private fun setupAuth() {
    auth = Firebase.auth
    firebaseUser = auth.currentUser

    if (firebaseUser == null) {
      startActivity(Intent(this, LoginActivity::class.java))
      finish()
      return
    }
  }

  private fun sendChat() {

    val friendlyMessage = Message(
      binding.messageEditText.text.toString(),
      firebaseUser?.displayName.toString(),
      firebaseUser?.photoUrl.toString(),
      Date().time,
    )

    // insert model ke database
    messagesRef.push().setValue(friendlyMessage) { error, _ ->
      if (error != null) {
        Toast.makeText(this, getString(R.string.send_error) + error.message, Toast.LENGTH_SHORT)
          .show()
      } else {
        Toast.makeText(this, getString(R.string.send_success), Toast.LENGTH_SHORT).show()
      }
    }
    binding.messageEditText.setText("")
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.main_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.sign_out_menu -> {
        signOut()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun signOut() {
    auth.signOut()
    startActivity(Intent(this, LoginActivity::class.java))
    finish()
  }

  override fun onResume() {
    super.onResume()
    adapter.startListening()
  }

  override fun onPause() {
    adapter.stopListening()
    super.onPause()
  }

  companion object {
    const val MESSAGES_CHILD = "messages"
  }
}