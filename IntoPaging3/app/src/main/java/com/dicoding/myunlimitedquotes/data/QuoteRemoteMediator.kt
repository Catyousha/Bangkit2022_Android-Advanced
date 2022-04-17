package com.dicoding.myunlimitedquotes.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.myunlimitedquotes.database.QuoteDatabase
import com.dicoding.myunlimitedquotes.database.RemoteKeys
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

// untuk menentukan laman mana yang akan di load
// berdasarkan pertimbangan database dan apiService
@OptIn(ExperimentalPagingApi::class)
class QuoteRemoteMediator(
  private val database: QuoteDatabase,
  private val apiService: ApiService,
) : RemoteMediator<Int, QuoteResponseItem>() {

  override suspend fun initialize(): InitializeAction {
    return InitializeAction.LAUNCH_INITIAL_REFRESH
  }

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, QuoteResponseItem>
  ): MediatorResult {
    // menentukan page berapa yang akan ditampilkan
    val page = when (loadType) {

      LoadType.REFRESH -> {
        val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
        remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
      }

      LoadType.PREPEND -> {
        val remoteKeys = getRemoteKeyForFirstItem(state)
        val prevKey = remoteKeys?.prevKey
          ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
        prevKey
      }

      LoadType.APPEND -> {
        val remoteKeys = getRemoteKeyForLastItem(state)
        val nextKey = remoteKeys?.nextKey
          ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
        nextKey
      }
    }

    return try {
      val responseData = apiService.getQuote(page, state.config.pageSize)
      val endOfPaginationReached = responseData.isEmpty()

      database.withTransaction {

        // bersih-bersih duls
        if (loadType == LoadType.REFRESH) {
          database.remoteKeysDao().deleteRemoteKeys()
          database.quoteDao().deleteAll()
        }

        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (endOfPaginationReached) null else page + 1

        // menentukan data-data quote yang akan ditampilkan berada di posisi mana
        // berdasarkan page sebelum dan sesudahnya

        val keys = responseData.map {
          RemoteKeys(
            it.id,
            prevKey,
            nextKey
          )
        }
        database.apply {
          remoteKeysDao().insertAll(keys)
          quoteDao().insertQuote(responseData)
        }
      }

      MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
    } catch (e: Exception) {
      MediatorResult.Error(e)
    }
  }

  private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
    return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let {
      database.remoteKeysDao().getRemoteKeysId(it.id)
    }
  }

  private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
    return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let {
      database.remoteKeysDao().getRemoteKeysId(it.id)
    }
  }

  private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
    return state.anchorPosition?.let { position ->
      state.closestItemToPosition(position)?.id?.let { id ->
        database.remoteKeysDao().getRemoteKeysId(id)
      }
    }
  }

  companion object {
    const val INITIAL_PAGE_INDEX = 1
  }

}