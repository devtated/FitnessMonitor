package com.example.fitnessmonitor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnessmonitor.data.PreferencesManager
import com.example.fitnessmonitor.data.UserProfile

@Composable
fun ProfileScreen(preferencesManager: PreferencesManager) {
    val context = LocalContext.current
    val savedProfile = preferencesManager.getProfile()

    var name by remember { mutableStateOf(savedProfile.name) }
    var age by remember { mutableStateOf(if (savedProfile.age > 0) savedProfile.age.toString() else "") }
    var gender by remember { mutableStateOf(savedProfile.gender) }
    var height by remember { mutableStateOf(if (savedProfile.height > 0f) savedProfile.height.toString() else "") }
    var weight by remember { mutableStateOf(if (savedProfile.weight > 0f) savedProfile.weight.toString() else "") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "User Profile Management",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age (years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender (Male/Female/Other)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                if (successMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = successMessage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Validation
                        if (name.length < 2 || !name.matches(Regex("[A-Za-z .'-]+"))) {
                            errorMessage = "Name must contain at least 2 letters."
                            successMessage = ""
                            return@Button
                        }
                        val ageInt = age.toIntOrNull()
                        if (ageInt == null || ageInt < 13 || ageInt > 120) {
                            errorMessage = "Please enter a valid age (13-120)."
                            successMessage = ""
                            return@Button
                        }
                        if (gender.isBlank()) {
                            errorMessage = "Please enter a gender option."
                            successMessage = ""
                            return@Button
                        }
                        val heightFloat = height.toFloatOrNull()
                        if (heightFloat == null || heightFloat < 80f || heightFloat > 250f) {
                            errorMessage = "Please enter a valid height in cm (80-250)."
                            successMessage = ""
                            return@Button
                        }
                        val weightFloat = weight.toFloatOrNull()
                        if (weightFloat == null || weightFloat < 20f || weightFloat > 350f) {
                            errorMessage = "Please enter a valid weight in kg (20-350)."
                            successMessage = ""
                            return@Button
                        }

                        errorMessage = ""
                        val profile = UserProfile(name, ageInt, gender, heightFloat, weightFloat)
                        preferencesManager.saveProfile(profile)
                        successMessage = "Profile saved successfully!"
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Profile")
                }
            }
        }
    }
}
