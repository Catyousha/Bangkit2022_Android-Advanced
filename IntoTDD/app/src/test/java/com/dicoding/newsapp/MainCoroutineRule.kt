package com.dicoding.newsapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// untuk melangsungkan test yang membutuhkan coroutine
@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
  TestWatcher(),
  TestCoroutineScope by TestCoroutineScope(dispatcher) {

  // setiap test dijalankan, akan dijalankan
  override fun starting(description: Description?) {
    super.starting(description)
    Dispatchers.setMain(dispatcher)
  }

  // setelah test selesai, akan dijalankan
  override fun finished(description: Description?) {
    super.finished(description)
    cleanupTestCoroutines()
    Dispatchers.resetMain()
  }
}