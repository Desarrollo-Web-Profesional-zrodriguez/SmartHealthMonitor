package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import mx.utng.smarthealthmonitor.tv.presentation.TvCatalogScreen
import mx.utng.smarthealthmonitor.tv.presentation.TvDetailScreen
import mx.utng.smarthealthmonitor.tv.presentation.TvPlaybackScreen
import mx.utng.smarthealthmonitor.tv.ui.theme.SmartHealthTvTheme

class TVActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHealthTvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    TVNavHost()
                }
            }
        }
    }
}

@Composable
fun TVNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "catalog") {
        composable("catalog") {
            TvCatalogScreen(onCardClick = { lecturaId ->
                navController.navigate("detail/$lecturaId")
            })
        }
        composable(
            route = "detail/{lecturaId}",
            arguments = listOf(navArgument("lecturaId") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("lecturaId") ?: return@composable
            TvDetailScreen(lecturaId = id, navController = navController)
        }
        composable("playback") {
            TvPlaybackScreen(navController = navController)
        }
    }
}
