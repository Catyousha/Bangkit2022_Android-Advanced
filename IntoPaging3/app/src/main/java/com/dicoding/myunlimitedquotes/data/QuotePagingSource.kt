package com.dicoding.myunlimitedquotes.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

// fetching api data dan membuat paging source
class QuotePagingSource(private val apiService: ApiService) :
  PagingSource<Int, QuoteResponseItem>() {

  // untuk menentukan laman keberapa data selanjutnya akan di load
  override fun getRefreshKey(state: PagingState<Int, QuoteResponseItem>): Int? {
    return state.anchorPosition?.let {
      val anchorPage = state.closestPageToPosition(it)
      anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
  }

  // load data dari api dan menentukan jenis resultnya
  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuoteResponseItem> {
    return try {
      // dapatkan posisi laman yang akan dibuka
      val position = params.key ?: INITIAL_PAGE_INDEX

      // ambil data dari api
      val responseData = apiService.getQuote(position, params.loadSize)

      LoadResult.Page(
        data = responseData,
        prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
        nextKey = if (responseData.isEmpty()) null else position + 1
      )
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  private companion object {
    const val INITIAL_PAGE_INDEX = 1
  }
}