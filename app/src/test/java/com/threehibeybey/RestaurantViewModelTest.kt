package com.threehibeybey

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.threehibeybey.models.SchoolCanteen
import com.threehibeybey.repositories.RestaurantRepository
import com.threehibeybey.viewmodels.RestaurantViewModel
import com.threehibeybey.utils.JsonLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `loadCanteens successfully updates canteens`() = runTest {
        val mockRepo = mock(RestaurantRepository::class.java)
        val mockJsonLoader = mock(JsonLoader::class.java)
        val mockContext = mock(android.content.Context::class.java)

        val sampleCanteens = listOf(
            SchoolCanteen(name = "至善餐廳", items = emptyList()),
            SchoolCanteen(name = "其他餐廳", items = emptyList())
        )

        `when`(mockRepo.loadCanteens(mockJsonLoader, mockContext)).thenReturn(sampleCanteens)

        val viewModel = RestaurantViewModel(mockRepo)
        viewModel.loadCanteens(mockJsonLoader, mockContext)

        val canteens = viewModel.canteens.value
        assertEquals(2, canteens.size)
        assertEquals("至善餐廳", canteens[0].name)
        assertEquals("其他餐廳", canteens[1].name)
    }

    @Test
    fun `augmentCanteens adds FamilyMart when ZhiShanCanteen exists`() = runTest {
        val mockRepo = mock(RestaurantRepository::class.java)
        val viewModel = RestaurantViewModel(mockRepo)

        viewModel.loadCanteens(JsonLoader(), mock(android.content.Context::class.java))
        viewModel.augmentCanteens()

        // Assuming initial canteens contain "至善餐廳"
        viewModel._canteens.value = listOf(
            SchoolCanteen(name = "至善餐廳", items = emptyList())
        )
        viewModel.augmentCanteens()

        val canteens = viewModel.canteens.value
        assertEquals(2, canteens.size)
        assertEquals("至善餐廳", canteens[0].name)
        assertEquals("全家便利商店", canteens[1].name)
    }
}
