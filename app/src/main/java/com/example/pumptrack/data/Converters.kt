package com.example.pumptrack.data

import androidx.room.TypeConverter
import com.example.pumptrack.data.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromExerciseList(value: MutableList<Exercise>): String {
        val gson = Gson()
        val type = object : TypeToken<MutableList<Exercise>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toExerciseList(value: String): MutableList<Exercise> {
        val gson = Gson()
        val type = object : TypeToken<MutableList<Exercise>>() {}.type
        return gson.fromJson(value, type)
    }
}