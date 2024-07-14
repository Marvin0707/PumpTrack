package com.example.pumptrack.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.pumptrack.AddFab
import com.example.pumptrack.BottomBar
import com.example.pumptrack.data.Workout
import com.example.pumptrack.data.WorkoutViewModel

@Composable
fun WorkoutListScreen(
    currentScreen: MutableState<SCREENS>,
    currentWorkout: MutableState<Workout>,
    workoutViewModel: WorkoutViewModel
) {
    val workouts by workoutViewModel.workouts.collectAsState(initial = emptyList())
    val showDialog = remember { mutableStateOf(false) }


    Scaffold(
        floatingActionButton = { AddFab(showDialog) },
        bottomBar = { BottomBar(currentScreen) },
        modifier = Modifier.fillMaxSize()
    ) {
        WorkoutList(workouts = workouts, it, currentScreen, currentWorkout, workoutViewModel)
        if (showDialog.value) {
            AddWorkoutDialog(showDialog = showDialog, workoutViewModel)
        }
    }
}

@Composable
fun WorkoutList(
    workouts: List<Workout>,
    innerPadding: PaddingValues,
    currentScreen: MutableState<SCREENS>,
    currentWorkout: MutableState<Workout>,
    workoutViewModel: WorkoutViewModel
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(innerPadding).padding(top = 32.dp)
    ) {
        items(workouts) {
            WorkoutListItem(it, currentScreen, currentWorkout,workoutViewModel)
        }
    }
}

@Composable
fun WorkoutListItem(
    workout: Workout,
    currentScreen: MutableState<SCREENS>,
    currentWorkout: MutableState<Workout>,
    workoutViewModel: WorkoutViewModel
) {

    var expanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.2f)
            .clickable {
                currentScreen.value = SCREENS.WORKOUT_DETAIL
                currentWorkout.value = workout
            }
    ) {
        Text(
            text = workout.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Box {
            IconButton(onClick = { expanded = !expanded }) {

                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "more")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(0.dp, 1.dp)
            ) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    workoutViewModel.deleteWorkout(workout)
                }, text = { Text(text = "Delete") })
            }
        }
    }
    HorizontalDivider()

}

@Composable
fun AddWorkoutDialog(showDialog: MutableState<Boolean>,workoutViewModel: WorkoutViewModel) {

    val name = remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(onDismissRequest = { showDialog.value = false }, confirmButton = {
        Button(onClick = {
            workoutViewModel.addWorkout(Workout(0, name.value.text))
            showDialog.value = false
            name.value = TextFieldValue("")
        }) {
            Text(text = "Add")
        }
    },
        title = { Text(text = "Add workout") },
        text = {
            TextField(value = name.value, onValueChange = { newName -> name.value = newName })
        })
}