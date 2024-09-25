package com.example.pumptrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(workout: Workout)

    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Delete
    fun delete(workout: Workout)

    @Insert
    fun addExercise(exercise: Exercise)

    @Delete
    fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    fun getExercisesForWorkout(workoutId: Int): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises GROUP BY name")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name = :name")
    fun getExerciseByName(name: String): Flow<List<Exercise>>

    @Update
    fun updateExercisePosition(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSet(set: Set)

    @Delete
    fun deleteSet(set: Set)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId")
    fun getSetsForExercise(exerciseId: Int): Flow<List<Set>>

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY setId DESC LIMIT 5")
    fun getLastFiveSetsForExercise(exerciseId: Int): Flow<List<Set>>

    @Query("SELECT * FROM sets")
    fun getAllSets(): Flow<List<Set>>
}
