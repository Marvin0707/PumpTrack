package com.example.pumptrack.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.example.pumptrack.screens.SCREENS

@Composable
fun BottomBar(currentScreen: MutableState<SCREENS>) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen.value == SCREENS.WORKOUTS,
            onClick = { currentScreen.value = SCREENS.WORKOUTS },
            icon = { Icon(Icons.Default.FitnessCenter, "") },
            label = { Text(text = "Workouts") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = currentScreen.value == SCREENS.STATISTICS,
            onClick = { currentScreen.value = SCREENS.STATISTICS },
            icon = { Icon(Icons.Default.BarChart, "") },
            label = { Text(text = "Statistics") },
            alwaysShowLabel = false
        )

    }
}