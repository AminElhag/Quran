package com.quran.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quran.app.data.QuranRepository
import com.quran.app.ui.screens.SearchScreen
import com.quran.app.ui.screens.SurahListScreen
import com.quran.app.ui.screens.SurahReadingScreen

sealed class Screen(val route: String) {
    data object SurahList : Screen("surah_list")
    data object SurahReading : Screen("surah_reading/{surahNumber}") {
        fun createRoute(surahNumber: Int) = "surah_reading/$surahNumber"
    }
    data object Search : Screen("search")
}

@Composable
fun QuranNavGraph(
    navController: NavHostController
) {
    val repository = remember { QuranRepository() }

    NavHost(
        navController = navController,
        startDestination = Screen.SurahList.route
    ) {
        composable(Screen.SurahList.route) {
            SurahListScreen(
                repository = repository,
                onSurahClick = { surahNumber ->
                    navController.navigate(Screen.SurahReading.createRoute(surahNumber))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
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
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                repository = repository,
                onBackClick = { navController.popBackStack() },
                onSurahClick = { surahNumber ->
                    navController.navigate(Screen.SurahReading.createRoute(surahNumber)) {
                        popUpTo(Screen.SurahList.route)
                    }
                }
            )
        }
    }
}
