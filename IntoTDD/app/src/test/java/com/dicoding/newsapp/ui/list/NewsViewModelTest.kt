package com.dicoding.newsapp.ui.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.newsapp.DataDummy
import com.dicoding.newsapp.data.NewsRepository
import com.dicoding.newsapp.data.Result
import com.dicoding.newsapp.data.local.entity.NewsEntity
import com.dicoding.newsapp.getOrAwaitValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NewsViewModelTest {

  // paksa agar seluruh task berjalan secara sinkronus
  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  // mock: objek bukan yang sebenarnya
  @Mock
  private lateinit var newsRepository: NewsRepository

  private lateinit var newsViewModel: NewsViewModel
  private val dummyNews = DataDummy.generateDummyNewsEntity()

  @Before
  fun setUp() {
    newsViewModel = NewsViewModel(newsRepository)
  }

  @Test
  fun `when Get HeadlineNews Should Not Null and Return Success`() {
    // expected news diinisialisasi dalam bentuk mutablelivedata
    val expectedNews = MutableLiveData<Result<List<NewsEntity>>>()
    expectedNews.value = Result.Success(dummyNews)

    // mocking hasil keluaran repository
    `when`(newsRepository.getHeadlineNews()).thenReturn(expectedNews)

    // test, dipakai getOrAwaitValue untuk mengambil value dari livedata (ekstensi tambahan)
    val actualNews = newsViewModel.getHeadlineNews().getOrAwaitValue()

    // memastikan apakah newsRepository memicu method getHeadlineNews
    Mockito.verify(newsRepository).getHeadlineNews()

    // hasil tidak null
    Assert.assertNotNull(actualNews)
    // hasil bertipe Result.Success
    Assert.assertTrue(actualNews is Result.Success)
    // keluaran sama dengan yang diinginkan
    Assert.assertEquals(dummyNews.size, (actualNews as Result.Success).data.size)
  }
}