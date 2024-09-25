package com.example.pumptrack.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.pumptrack.data.Exercise
import com.example.pumptrack.data.Set
import com.example.pumptrack.data.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WorkoutLogScreen(
    currentScreen: MutableState<SCREENS>,
    workoutViewModel: WorkoutViewModel
) {
    val numberOfSets = remember {
        mutableStateListOf<Int>()
    }
    val exercises by workoutViewModel.exercises.collectAsState()
    exercises.forEach { _ ->
        numberOfSets.add(4)
    }
    val selectedExercise = remember {
        mutableStateOf(exercises.find { it.index == 0 }?.name ?: exercises.first().name)
    }
    val indexOfExercise = remember {
        mutableIntStateOf(exercises.indexOfFirst { it.name == selectedExercise.value })
    }
    val weights = remember {
        mutableStateListOf<MutableList<MutableState<TextFieldValue>>>()
    }
    exercises.forEach { _ ->
        weights.add(mutableListOf())
    }
    val reps = remember {
        mutableStateListOf<MutableList<MutableState<TextFieldValue>>>()
    }
    exercises.forEach { _ ->
        reps.add(mutableListOf())
    }
    val finished = remember {
        mutableStateListOf<MutableList<MutableState<Boolean>>>()
    }
    exercises.forEach { _ ->
        finished.add(mutableListOf())
    }

    val setStates = remember { mutableStateListOf<MutableList<Set>>() }
    exercises.forEach { _ ->
        setStates.add(mutableListOf())
    }

    BackHandler {
        //do nothing
    }

    Scaffold(floatingActionButton = { AddSet(numberOfSets, indexOfExercise) }, bottomBar = {
        ExerciseChips(
            exercises = exercises.toMutableList(),
            selectedExercise = selectedExercise,
            currentScreen,
            finished,
            setStates,
            workoutViewModel
        )
    }) { it ->
        LaunchedEffect(selectedExercise.value) {
            indexOfExercise.intValue =
                exercises.indexOfFirst { it.name == selectedExercise.value }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LastSetsList(
                indexOfExercise = indexOfExercise,
                exercises = exercises,
                workoutViewModel = workoutViewModel
            )
            SetList(
                it,
                numberOfSets,
                setStates,
                indexOfExercise,
                weights,
                reps,
                finished,
                exercises
            )
        }
    }
}

@Composable
fun ExerciseChips(
    exercises: MutableList<Exercise>,
    selectedExercise: MutableState<String>,
    currentScreen: MutableState<SCREENS>,
    finished: SnapshotStateList<MutableList<MutableState<Boolean>>>,
    setStates: SnapshotStateList<MutableList<Set>>,
    workoutViewModel: WorkoutViewModel
) {
    BottomAppBar {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(exercises.sortedBy { it.index }) { index, it ->
                InputChip(
                    selected = it.name == selectedExercise.value,
                    onClick = { selectedExercise.value = it.name },
                    label = { Text(text = it.name) },
                    trailingIcon = {
                        if (finished[index].any { it.value }) Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "check"
                        )
                    })
            }
            item {
                TextButton(onClick = {
                    setStates.forEach {
                        it.forEach {
                            workoutViewModel.addSet(it)
                        }
                    }
                    currentScreen.value = SCREENS.WORKOUTS
                }) {
                    Text(text = "End Workout")
                }
            }
        }
    }
}

@Composable
fun ExerciseLogRow(
    setStates: SnapshotStateList<MutableList<Set>>,
    indexOfExercise: MutableIntState,
    index: Int,
    weights: SnapshotStateList<MutableList<MutableState<TextFieldValue>>>,
    reps: SnapshotStateList<MutableList<MutableState<TextFieldValue>>>,
    finished: SnapshotStateList<MutableList<MutableState<Boolean>>>,
    exercises: List<Exercise>
) {

    if (weights[indexOfExercise.intValue].size <= index) {
        weights[indexOfExercise.intValue].add(index, remember {
            mutableStateOf(TextFieldValue("0"))
        })
        reps[indexOfExercise.intValue].add(index, remember {
            mutableStateOf(TextFieldValue(""))
        })
        finished[indexOfExercise.intValue].add(remember {
            mutableStateOf(false)
        })
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}",
            modifier = Modifier.weight(0.05f)
        )
        OutlinedTextField(
            value = weights[indexOfExercise.intValue][index].value,
            onValueChange = { newWeight ->
                weights[indexOfExercise.intValue][index].value = newWeight
            },
            modifier = Modifier.weight(0.1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text(text = "kg") },
            singleLine = true,
            enabled = !finished[indexOfExercise.intValue][index].value

        )
        OutlinedTextField(
            value = reps[indexOfExercise.intValue][index].value,
            onValueChange = { newReps -> reps[indexOfExercise.intValue][index].value = newReps },
            modifier = Modifier.weight(0.1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Reps") },
            singleLine = true,
            enabled = !finished[indexOfExercise.intValue][index].value

        )
        Checkbox(
            checked = finished[indexOfExercise.intValue][index].value,
            onCheckedChange = {
                finished[indexOfExercise.intValue][index].value =
                    !finished[indexOfExercise.intValue][index].value
                if (finished[indexOfExercise.intValue][index].value) {
                    setStates[indexOfExercise.intValue].add(
                        Set(
                            0,
                            weights[indexOfExercise.intValue][index].value.text.toDouble(),
                            reps[indexOfExercise.intValue][index].value.text.toInt(),
                            getCurrentDateAsString(),
                            exercises[indexOfExercise.intValue].id

                        )
                    )
                } else {
                    setStates[indexOfExercise.intValue].removeAt(index)
                }
            },
            modifier = Modifier.weight(0.05f)
        )
    }
}

@Composable
fun AddSet(numberOfSets: SnapshotStateList<Int>, indexOfExercise: MutableIntState) {
    FloatingActionButton(onClick = { numberOfSets[indexOfExercise.intValue]++ }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
    }
}

@Composable
fun SetList(
    paddingValues: PaddingValues,
    numberOfSets: SnapshotStateList<Int>,
    setStates: SnapshotStateList<MutableList<Set>>,
    indexOfExercise: MutableIntState,
    weights: SnapshotStateList<MutableList<MutableState<TextFieldValue>>>,
    reps: SnapshotStateList<MutableList<MutableState<TextFieldValue>>>,
    finished: SnapshotStateList<MutableList<MutableState<Boolean>>>,
    exercises: List<Exercise>
) {
    Box(
        Modifier
            .padding(bottom = paddingValues.calculateBottomPadding(), top = 8.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            Modifier
                .align(Alignment.TopCenter)
        ) {
            items(numberOfSets[indexOfExercise.intValue]) {
                ExerciseLogRow(
                    setStates = setStates,
                    indexOfExercise = indexOfExercise,
                    index = it,
                    weights,
                    reps,
                    finished,
                    exercises
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun LastSetsList(
    indexOfExercise: MutableIntState,
    exercises: List<Exercise>,
    workoutViewModel: WorkoutViewModel
) {
    val setsFlow = workoutViewModel.getLast5SetsForExercise(exercises[indexOfExercise.intValue])
        .collectAsState(initial = emptyList())

    // Observe the setsFlow and update the UI when it changes
    val sets by setsFlow
    Column(
        modifier = Modifier
            .padding(top = 30.dp, start = 12.dp)
    ) {
        if (sets.isNotEmpty()) {
            Text(text = "Last 5")
        }
        sets.reversed().forEach { set ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = set.date + ":", fontWeight = FontWeight(300))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = set.weight.toString() + "kg", fontWeight = FontWeight(300))
                    Text(text = "x", fontWeight = FontWeight(300))
                    Text(text = set.reps.toString(), fontWeight = FontWeight(300))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDateAsString(): String {
    val dateFormat = SimpleDateFormat("dd.MM.yy")
    val currentDate = Date()

    return dateFormat.format(currentDate)
}
