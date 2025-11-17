package com.quran.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quran.app.data.QuranRepository
import com.quran.app.data.ReadingPositionManager
import com.quran.app.data.Settings
import com.quran.app.data.TextSizeManager
import com.quran.app.ui.screens.PageReadingScreen
import com.quran.app.ui.screens.SearchScreen
import com.quran.app.ui.screens.SurahListScreen
import com.quran.app.ui.screens.SurahReadingScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object SurahList : Screen("surah_list")
    data object SurahReading : Screen("surah_reading/{surahNumber}") {
        fun createRoute(surahNumber: Int) = "surah_reading/$surahNumber"
    }
    data object PageReading : Screen("page_reading/{pageNumber}") {
        fun createRoute(pageNumber: Int = 1) = "page_reading/$pageNumber"
    }
    data object Search : Screen("search")
}

@Composable
fun QuranNavGraph(
    navController: NavHostController
) {
    val repository = remember { QuranRepository() }
    val settings = remember { Settings() }
    val readingPositionManager = remember { ReadingPositionManager(settings) }
    val textSizeManager = remember { TextSizeManager(settings) }
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.SurahList.route
    ) {
        composable(Screen.SurahList.route) {
            SurahListScreen(
                repository = repository,
                readingPositionManager = readingPositionManager,
                onSurahClick = { surahNumber ->
                    // Navigate to book/page reading style by default
                    scope.launch {
                        val pageNumber = repository.getPageForSurah(surahNumber).first()
                        navController.navigate(Screen.PageReading.createRoute(pageNumber))
                    }
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onPageReadingClick = { pageNumber ->
                    navController.navigate(Screen.PageReading.createRoute(pageNumber))
                }
            )
        }

        composable(
            route = Screen.SurahReading.route,
            arguments = listOf(
                navArgument("surahNumber") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
            SurahReadingScreen(
                surahNumber = surahNumber,
                repository = repository,
                readingPositionManager = readingPositionManager,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                repository = repository,
                onBackClick = { navController.popBackStack() },
                onSurahClick = { surahNumber ->
                    // Navigate to book/page reading style by default
                    scope.launch {
                        val pageNumber = repository.getPageForSurah(surahNumber).first()
                        navController.navigate(Screen.PageReading.createRoute(pageNumber)) {
                            popUpTo(Screen.SurahList.route)
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.PageReading.route,
            arguments = listOf(
                navArgument("pageNumber") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val pageNumber = backStackEntry.arguments?.getInt("pageNumber") ?: 1
            PageReadingScreen(
                initialPage = pageNumber,
                repository = repository,
                readingPositionManager = readingPositionManager,
                textSizeManager = textSizeManager,
                onBackClick = { navController.popBackStack() },
                onSurahListClick = {
                    navController.navigate(Screen.SurahList.route) {
                        popUpTo(Screen.SurahList.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
