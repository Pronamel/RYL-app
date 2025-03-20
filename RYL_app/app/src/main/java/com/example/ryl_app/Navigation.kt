package com.example.ryl_app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Call the function to create the directory
        val rylDirectory = createRYLDirectory(applicationContext)
        Log.d("RYL_Directory", "Directory created at: ${rylDirectory.absolutePath}")

        // Register the permission launcher
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permission", "Microphone permission granted.")
            } else {
                Log.d("Permission", "Microphone permission denied.")
                // Optionally, show a dialog explaining why the permission is needed.
            }
        }

        // Check and request microphone permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            Log.d("Permission", "Microphone permission already granted.")
        }

        setContent {
            MyApp()
        }
    }
}








@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}
//=====================================================================================



sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateModule : Screen("create_module")

    object InsideModule : Screen("inside_module/{path}/{duration}") {
        fun createRoute(path: String, duration: Int) = "inside_module/$path/$duration"
    }

    object InsideDay : Screen("inside_day/{duration}/{week}/{day}/{moduleName}") {
        fun createRoute(duration: Int, week: Int, day: String, moduleName: String) = "inside_day/$duration/$week/$day/$moduleName"
    }

    object LectureBuilder : Screen("lecture_builder/{week}/{duration}/{moduleName}/{day}") {
        fun createRoute(week: Int, duration: Int, moduleName: String, day: String) = "lecture_builder/$week/$duration/$moduleName/$day"
    }

    object InsideLecture : Screen("inside_lecture/{day}/{week}/{name}/{moduleName}") {
        fun createRoute(day: String, week: Int, name: String, moduleName: String) = "inside_lecture/$day/$week/$name/$moduleName"
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(
            route = Screen.InsideModule.route,
            arguments = listOf(
                navArgument("path") { type = NavType.StringType },
                navArgument("duration") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: "DefaultPath"
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 60

            InsideAModuleScreen(
                path = path,
                duration = duration,
                BackToHome = { navController.navigate(Screen.Home.route) },
                NavigateToDay = { duration, week, day, moduleName ->
                    navController.navigate(Screen.InsideDay.createRoute(duration, week, day, moduleName))
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSecondScreen = { navController.navigate(Screen.CreateModule.route) },
                onNavigateToInsideModule = { path, duration ->
                    navController.navigate(Screen.InsideModule.createRoute(path, duration))
                }
            )
        }

        composable(Screen.CreateModule.route) {
            createModuleScreen(
                ExitSelection = { navController.popBackStack() },
                ConfirmSelection = { navController.navigate(Screen.InsideModule.route) },
                onNavigateToInsideModule = { path, duration ->
                    navController.navigate(Screen.InsideModule.createRoute(path, duration))
                }
            )
        }

        composable(
            route = Screen.InsideDay.route,
            arguments = listOf(
                navArgument("duration") { type = NavType.StringType },
                navArgument("week") { type = NavType.IntType },  // Changed to IntType
                navArgument("day") { type = NavType.StringType },
                navArgument("moduleName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 60
            val week = backStackEntry.arguments?.getInt("week") ?: 1  // Changed to getInt()
            val day = backStackEntry.arguments?.getString("day") ?: "DefaultDay"
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: "DefaultModule"

            Log.d("InsideADayScreen", "week: $week, day: $day, moduleName: $moduleName")

            InsideADayScreen(
                duration = duration,
                week = week,
                day = day,
                moduleName = moduleName,
                BackToModule = { navController.popBackStack() },
                ToLectureBuilder = { week, duration, moduleName, day ->
                    navController.navigate(Screen.LectureBuilder.createRoute(week, duration, moduleName, day))
                },
                ToLecture = { day, week, name, moduleName ->
                    navController.navigate(Screen.InsideLecture.createRoute(day, week, name, moduleName))
                }
            )
        }

        composable(Screen.LectureBuilder.route) { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: "DefaultDay"
            val name = backStackEntry.arguments?.getString("name") ?: "DefaultName"
            val week = backStackEntry.arguments?.getInt("week") ?: 1  // Changed to getInt()
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: "DefaultModule"

            LectureBuilderScreen(
                day = day,
                week = week,
                name = name,
                moduleName = moduleName,
                BackToDay = { navController.popBackStack() },
                ToLecture = { day, week,  name, moduleName ->
                    navController.navigate(Screen.InsideLecture.createRoute(day, week,  name, moduleName))
                }
            )
        }

        composable(
            route = Screen.InsideLecture.route,
            arguments = listOf(
                navArgument("day") { type = NavType.StringType },
                navArgument("week") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType },
                navArgument("moduleName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: "DefaultDay"
            val week = backStackEntry.arguments?.getInt("week") ?: 1  // Changed to getInt()
            val name = backStackEntry.arguments?.getString("name") ?: "DefaultName"
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: "DefaultModule"

            InsideALectureScreen(
                day = day,
                week = week,
                name = name,
                moduleName = moduleName,
                BackToLectureBuilder = { navController.popBackStack() }
            )
        }
    }
}
