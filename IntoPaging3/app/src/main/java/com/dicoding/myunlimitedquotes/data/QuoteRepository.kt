package com.dicoding.myunlimitedquotes.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.myunlimitedquotes.database.QuoteDatabase
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

class QuoteRepository(
  private val quoteDatabase: QuoteDatabase,
  private val apiService: ApiService
) {

  // pagination data otomatis
  @OptIn(ExperimentalPagingApi::class)
  fun getQuote(): LiveData<PagingData<QuoteResponseItem>> {
    return Pager(
      // jumlah data per laman
      config = PagingConfig(
        pageSize = 5,
      ),
      remoteMediator = QuoteRemoteMediator(quoteDatabase, apiService),
      pagingSourceFactory = {
        // data source yang digunakan hanya dari database
        // QuotePagingSource(apiService)
        quoteDatabase.quoteDao().getAllQuote()
      }
    ).liveData
  }
}