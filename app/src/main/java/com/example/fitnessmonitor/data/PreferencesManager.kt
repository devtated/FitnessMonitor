package com.example.fitnessmonitor.data

import android.content.Context
import android.content.SharedPreferences

data class UserProfile(
    val name: String,
    val age: Int,
    val gender: String,
    val height: Float, // in cm
    val weight: Float  // in kg
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString("profile_name", profile.name)
            putInt("profile_age", profile.age)
            putString("profile_gender", profile.gender)
            putFloat("profile_height", profile.height)
            putFloat("profile_weight", profile.weight)
            apply()
        }
    }

    fun getProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("profile_name", "") ?: "",
            age = prefs.getInt("profile_age", 0),
            gender = prefs.getString("profile_gender", "Male") ?: "Male",
            height = prefs.getFloat("profile_height", 170f),
            weight = prefs.getFloat("profile_weight", 70f)
        )
    }

    fun saveGoal(goal: Int) {
        prefs.edit().putInt("daily_goal", goal).apply()
    }

    fun getGoal(): Int {
        return prefs.getInt("daily_goal", 5000)
    }
}
