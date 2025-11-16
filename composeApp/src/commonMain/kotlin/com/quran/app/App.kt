package com.quran.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.quran.app.navigation.QuranNavGraph
import com.quran.app.ui.theme.QuranTheme

@Composable
fun App() {
    QuranTheme {
        val navController = rememberNavController()
        QuranNavGraph(navController = navController)
    }
}
