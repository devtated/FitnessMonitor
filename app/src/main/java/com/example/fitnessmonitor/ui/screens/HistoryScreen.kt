package com.example.fitnessmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnessmonitor.data.ActivityRecord
import com.example.fitnessmonitor.data.FitnessDatabaseHelper
import java.util.Locale

@Composable
fun HistoryScreen(dbHelper: FitnessDatabaseHelper) {
    var records by remember { mutableStateOf(listOf<ActivityRecord>()) }

    LaunchedEffect(Unit) {
        records = dbHelper.getAllRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daily Activity Records (SQLite)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No previous activity records found in database.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    val hours = record.durationSeconds / 3600
                    val minutes = (record.durationSeconds % 3600) / 60
                    val seconds = record.durationSeconds % 60
                    val durationStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Date: ${record.date}", fontWeight = FontWeight.Bold)
                                Text(text = "${record.steps} Steps", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Active Duration: $durationStr", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
