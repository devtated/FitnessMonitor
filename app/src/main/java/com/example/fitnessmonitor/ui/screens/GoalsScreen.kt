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

@Composable
fun GoalsScreen(preferencesManager: PreferencesManager, onGoalUpdated: (Int) -> Unit) {
    val context = LocalContext.current
    var goalText by remember { mutableStateOf(preferencesManager.getGoal().toString()) }
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
            text = "Activity Goal Settings",
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set your daily target step goal. Saved securely with SharedPreferences.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Daily Step Goal") },
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
                        val goalInt = goalText.toIntOrNull()
                        if (goalInt == null || goalInt <= 0 || goalInt > 100000) {
                            errorMessage = "Please enter a valid step goal (1 - 100,000)."
                            successMessage = ""
                            return@Button
                        }
                        errorMessage = ""
                        preferencesManager.saveGoal(goalInt)
                        onGoalUpdated(goalInt)
                        successMessage = "Daily goal updated to $goalInt steps!"
                        Toast.makeText(context, "Goal saved", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Daily Goal")
                }
            }
        }
    }
}
