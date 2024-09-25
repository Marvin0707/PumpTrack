package com.example.pumptrack.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.pumptrack.composables.BottomBar
import com.example.pumptrack.data.Exercise
import com.example.pumptrack.data.Set
import com.example.pumptrack.data.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(currentScreen: MutableState<SCREENS>, workoutViewModel: WorkoutViewModel) {

    val exercises by workoutViewModel.getAllExercises().collectAsState(initial = listOf())
    val selectedText = remember { mutableStateOf(exercises.firstOrNull()?.name ?: "") }
    LaunchedEffect(exercises) {
        selectedText.value = exercises.firstOrNull()?.name.toString()
    }

    var pointsData by remember { mutableStateOf(listOf(Point(0f, 0f))) }
    var pointsData2 by remember { mutableStateOf(listOf(Point(0f, 0f))) }
    var dateLabels by remember { mutableStateOf(listOf("")) }
    var maxWeight by remember { mutableDoubleStateOf(0.0) }
    var maxWeight2 by remember { mutableDoubleStateOf(0.0) }

    val setsList = remember { mutableStateListOf<Set>() }

    LaunchedEffect(selectedText.value) {
        setsList.clear()
        workoutViewModel.getAllExercisesByName(selectedText.value).collect { e ->
            e.forEach {
                workoutViewModel.getSetsForExercise(it).collect {
                    setsList.addAll(it)
                }
            }
        }
    }

    // Reagiere auf Änderungen in setsList
    LaunchedEffect(Unit) {
        snapshotFlow { setsList.toList() }
            .distinctUntilChanged()
            .collectLatest { updatedSets ->
                var i = 1
                pointsData = listOf(Point(0f, 0f))
                pointsData2 = listOf(Point(0f, 0f))
                dateLabels = listOf("")
                maxWeight = 0.0
                maxWeight2 = 0.0
                updatedSets.forEach { set ->

                    //wenn es sets am selben tag gibt dann schaue ob dieser satz maximum ist und füge hinzu (damit nur max angezeigt wird)
                    val sameDay = updatedSets.filter { s -> s.date == set.date }
                    if (sameDay.isNotEmpty()) {
                        if (sameDay.maxBy { it.weight * it.reps } == set) {
                            if ((set.weight * set.reps) > maxWeight) maxWeight =
                                set.weight * set.reps
                            if (calc1RM(set.weight, set.reps) > maxWeight2) maxWeight2 =
                                calc1RM(set.weight, set.reps).toDouble()
                            pointsData =
                                pointsData + Point(i.toFloat(), (set.weight * set.reps).toFloat())
                            pointsData2 =
                                pointsData2 + Point(i.toFloat(), calc1RM(set.weight, set.reps))
                            dateLabels = dateLabels + set.date
                            i++
                        }
                    }
                }
            }
    }


    Scaffold(bottomBar = { BottomBar(currentScreen = currentScreen) }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (pointsData.isNotEmpty()) {
                MyLineChart(pointsData, dateLabels, maxWeight, pointsData2, maxWeight2)
            }
            ExposedDropdownMenuBox(selectedText, exercises)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBox(selectedText: MutableState<String>, exercises: List<Exercise>) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp)
    ) {
        ExposedDropdownMenuBox(
            modifier = Modifier.align(Alignment.Center),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText.value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                exercises.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item.name) },
                        onClick = {
                            selectedText.value = item.name
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MyLineChart(
    pointsData: List<Point>,
    dateLabels: List<String>,
    maxWeight: Double,
    pointsData2: List<Point>,
    maxWeight2: Double
) {
    val xAxisData = AxisData.Builder()
        .axisStepSize(60.dp)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .steps(pointsData.size - 1)
        .labelData { i -> if (i in dateLabels.indices) dateLabels[i] else "test" }
        .axisLabelAngle(15f)
        .labelAndAxisLinePadding(5.dp)
        .build()

    val xAxisData2 = AxisData.Builder()
        .axisStepSize(60.dp)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .steps(pointsData2.size - 1)
        .labelData { i -> if (i in dateLabels.indices) dateLabels[i] else "test" }
        .axisLabelAngle(15f)
        .labelAndAxisLinePadding(5.dp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(4)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .labelAndAxisLinePadding(25.dp)
        .labelData { index ->
            val yScale = (maxWeight / 4).roundToInt()
            (index * yScale).toString() + "kg"
        }.build()

    val yAxisData2 = AxisData.Builder()
        .steps(4)
        .axisLineColor(MaterialTheme.colorScheme.onBackground)
        .axisLabelColor(MaterialTheme.colorScheme.onBackground)
        .labelAndAxisLinePadding(25.dp)
        .labelData { index ->
            val yScale = (maxWeight2 / 4).roundToInt()
            (index * yScale).toString() + "kg"
        }.build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(color = MaterialTheme.colorScheme.primary),
                    IntersectionPoint(color = MaterialTheme.colorScheme.tertiary),
                    SelectionHighlightPoint(color = MaterialTheme.colorScheme.secondary),
                    ShadowUnderLine(color = MaterialTheme.colorScheme.primary),
                    SelectionHighlightPopUp(popUpLabel = { x, y -> y.toString() + "kg" })
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = MaterialTheme.colorScheme.background, paddingTop = 8.dp
    )

    val lineChartData2 = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData2,
                    LineStyle(color = MaterialTheme.colorScheme.primary),
                    IntersectionPoint(color = MaterialTheme.colorScheme.tertiary),
                    SelectionHighlightPoint(color = MaterialTheme.colorScheme.secondary),
                    ShadowUnderLine(color = MaterialTheme.colorScheme.primary),
                    SelectionHighlightPopUp(popUpLabel = { x, y -> y.toString() + "kg" })
                )
            ),
        ),
        xAxisData = xAxisData2,
        yAxisData = yAxisData2,
        gridLines = GridLines(),
        backgroundColor = MaterialTheme.colorScheme.background, paddingTop = 8.dp
    )

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f).padding(top = 16.dp)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Erstes Layout, das 50% des Platzes einnimmt
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Volume")
            LineChart(lineChartData = lineChartData, modifier = Modifier
                .fillMaxWidth())
        }

        // Zweites Layout, das die anderen 50% des Platzes einnimmt
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "1RM")
            LineChart(lineChartData = lineChartData2, modifier = Modifier
                .fillMaxWidth())
        }
    }

}

fun calc1RM(weight: Double, reps: Int): Float {
    return (weight * (1 + 0.033 * reps)).toFloat()
}