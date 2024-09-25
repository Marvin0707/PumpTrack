package com.example.pumptrack.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    val workouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> get() = _exercises

    fun addWorkout(workout: Workout) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                workoutDao.insert(workout)
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                workoutDao.delete(workout)
            }
        }
    }

    fun observeExercises(workout: Workout) {
        viewModelScope.launch {
            workoutDao.getExercisesForWorkout(workout.id)
                .collectLatest { exercises ->
                    if (_exercises.value != exercises) {
                        _exercises.value = exercises
                    }
                }
        }
    }

    suspend fun updateExercise(exercises: List<Exercise>) {
        withContext(Dispatchers.IO) {
            var i = 0
            exercises.forEach {
                val ex = it.copy(index = i)
                workoutDao.updateExercisePosition(ex)
                i++
            }
            withContext(Dispatchers.Main) {
                _exercises.value = exercises
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                workoutDao.addExercise(exercise)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                workoutDao.deleteExercise(exercise)
                val updatedExercises = _exercises.value.filter { it.id != exercise.id }
                updateExercise(updatedExercises)
            }
        }
    }


    fun addSet(set: Set) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                workoutDao.addSet(set)
            }
        }
    }

    fun getSetsForExercise(exercise: Exercise?): Flow<List<Set>> {
        return exercise?.let { workoutDao.getSetsForExercise(it.id) } ?: emptyFlow()
    }

    fun getLast5SetsForExercise(exercise: Exercise?): Flow<List<Set>> {
        return exercise?.let { workoutDao.getLastFiveSetsForExercise(it.id) } ?: emptyFlow()
    }

    fun getAllExercises(): Flow<List<Exercise>> {
        return workoutDao.getAllExercises()
    }

    fun getAllExercisesByName(name: String): Flow<List<Exercise>> {
        return workoutDao.getExerciseByName(name)
    }

    fun getAllSets(): Flow<List<Set>> {
        return workoutDao.getAllSets()
    }
}

class WorkoutViewModelFactory(
    private val workoutDao: WorkoutDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(workoutDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

