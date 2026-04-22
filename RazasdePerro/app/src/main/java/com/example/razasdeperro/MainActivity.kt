package com.example.razasdeperro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.razasdeperro.ui.screens.BreedDetailScreen
import com.example.razasdeperro.ui.screens.BreedListScreen
import com.example.razasdeperro.ui.viewmodel.DogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DogApp()
        }
    }
}

@Composable
fun DogApp() {
    val navController = rememberNavController()
    val viewModel: DogViewModel = viewModel()

    NavHost(navController = navController, startDestination = "breedList") {
        composable("breedList") {
            BreedListScreen(
                viewModel = viewModel,
                onBreedClick = { breedName ->
                    navController.navigate("breedDetail/$breedName")
                }
            )
        }
        composable(
            route = "breedDetail/{breedName}",
            arguments = listOf(navArgument("breedName") { type = NavType.StringType })
        ) { backStackEntry ->
            val breedName = backStackEntry.arguments?.getString("breedName") ?: ""
            BreedDetailScreen(viewModel = viewModel, breedName = breedName)
        }
    }
}
