package com.example.pumptrack.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pumptrack.data.Exercise
import com.example.pumptrack.data.Workout
import com.example.pumptrack.data.WorkoutViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun WorkoutDetailScreen(
    currentWorkout: MutableState<Workout>,
    currentScreen: MutableState<SCREENS>,
    workoutViewModel: WorkoutViewModel
) {
    val exercises by workoutViewModel.exercises.collectAsState()
    workoutViewModel.observeExercises(currentWorkout.value)

    val showDialog = remember { mutableStateOf(false) }

    BackHandler {
        currentScreen.value = SCREENS.WORKOUTS
    }


    Scaffold(
        floatingActionButton = { AddFab(showDialog, Modifier.padding(bottom = 80.dp)) },
        topBar = { TopBar(currentWorkout.value.name, currentScreen) },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Display the list of exercises
            ExerciseList(innerPadding = it, exercises, workoutViewModel)

            // Check if the dialog should be shown
            if (showDialog.value) {
                AddExerciseDialog(showDialog, currentWorkout, workoutViewModel, exercises)
            }

            // Sticky Button at the bottom center of the screen
            Button(
                onClick = { currentScreen.value = SCREENS.WORKOUT_LOG },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(it)
                    .padding(horizontal = 32.dp)
            ) {
                Text("Start")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseList(
    innerPadding: PaddingValues,
    exercises: List<Exercise>,
    workoutViewModel: WorkoutViewModel
) {
    val lazyListState = rememberLazyListState()

    val ex = remember {
        mutableStateOf(exercises)
    }
    LaunchedEffect(exercises) {
        ex.value = exercises.sortedBy { it.index }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        ex.value = ex.value.toMutableList().apply {
            this.add(to.index, removeAt(from.index))
        }
        workoutViewModel.updateExercise(ex.value)
    }

    ex.value.sortedBy { it.index }
    Log.d("sorted", ex.value.toString())
    LazyColumn(state = lazyListState, modifier = Modifier.padding(innerPadding)) {
        Log.d("test", "rendered")
        items(ex.value, key = { it.id }) { exercise ->
            ReorderableItem(reorderableLazyListState, key = exercise.id,Modifier.fillMaxHeight(0.15f)) { isDragging ->
                var expanded by remember { mutableStateOf(false) }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.08f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "drag",
                            modifier = Modifier.draggableHandle {}
                        )
                        Text(
                            text = exercise.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
                                workoutViewModel.deleteExercise(exercise)
                            }, text = { Text(text = "Delete") })
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun AddExerciseDialog(
    showDialog: MutableState<Boolean>,
    currentWorkout: MutableState<Workout>,
    workoutViewModel: WorkoutViewModel,
    exercises: List<Exercise>
) {

    val name = remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(onDismissRequest = { showDialog.value = false }, confirmButton = {
        Button(onClick = {
            val ex = Exercise(0, name.value.text, currentWorkout.value.id, exercises.size)
            workoutViewModel.addExercise(ex)
            name.value = TextFieldValue("")
            showDialog.value = false
        }) {
            Text(text = "Add")
        }
    },
        title = { Text(text = "Add exercise") },
        text = {
            TextField(value = name.value, onValueChange = { newName -> name.value = newName })
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(workoutName: String, currentScreen: MutableState<SCREENS>) {
    TopAppBar(title = { Text(text = workoutName) }, navigationIcon = {
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "back",
            modifier = Modifier.clickable { currentScreen.value = SCREENS.WORKOUTS }
        )
    })
}