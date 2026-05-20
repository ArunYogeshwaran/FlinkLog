package com.ayogeshwaran.workoutlogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayogeshwaran.workoutlogger.presentation.history.HistoryViewModel
import com.ayogeshwaran.workoutlogger.presentation.home.HomeViewModel
import com.ayogeshwaran.workoutlogger.presentation.navigation.AppNavigation
import com.ayogeshwaran.workoutlogger.presentation.theme.WorkoutLoggerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WorkoutLoggerApplication
        val container = app.container

        setContent {
            WorkoutLoggerTheme {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        addWorkoutUseCase = container.addWorkoutUseCase,
                        deleteWorkoutUseCase = container.deleteWorkoutUseCase,
                        getWorkoutsForDateUseCase = container.getWorkoutsForDateUseCase
                    )
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(
                        getWorkoutsForDateUseCase = container.getWorkoutsForDateUseCase,
                        getDatesWithWorkoutsUseCase = container.getDatesWithWorkoutsUseCase,
                        deleteWorkoutUseCase = container.deleteWorkoutUseCase,
                        addWorkoutUseCase = container.addWorkoutUseCase
                    )
                )

                AppNavigation(
                    homeViewModel = homeViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}

