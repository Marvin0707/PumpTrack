package com.example.pumptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pumptrack.data.AppDatabase
import com.example.pumptrack.data.Workout
import com.example.pumptrack.data.WorkoutViewModel
import com.example.pumptrack.data.WorkoutViewModelFactory
import com.example.pumptrack.screens.SCREENS
import com.example.pumptrack.screens.StatisticsScreen
import com.example.pumptrack.screens.WorkoutDetailScreen
import com.example.pumptrack.screens.WorkoutListScreen
import com.example.pumptrack.screens.WorkoutLogScreen
import com.example.pumptrack.ui.theme.PumpTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PumpTrackTheme {

                val currentScreen = remember { mutableStateOf(SCREENS.WORKOUTS) }
                val currentWorkout = remember { mutableStateOf(Workout(0,"Fehler")) }
                val workoutDao = AppDatabase.DatabaseBuilder.getInstance(this).workoutDao()
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(workoutDao)
                )
                when (currentScreen.value) {
                    SCREENS.WORKOUTS -> WorkoutListScreen(
                        currentScreen,
                        currentWorkout,
                        workoutViewModel
                    )
                    SCREENS.WORKOUT_DETAIL -> WorkoutDetailScreen(currentWorkout,currentScreen, workoutViewModel)
                    SCREENS.WORKOUT_LOG -> WorkoutLogScreen(currentScreen,workoutViewModel)
                    SCREENS.STATISTICS -> StatisticsScreen(currentScreen, workoutViewModel)
                }
            }
        }
    }
}
