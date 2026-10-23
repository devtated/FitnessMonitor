package com.example.fitnessmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.example.fitnessmonitor.data.FitnessDatabaseHelper
import com.example.fitnessmonitor.data.PreferencesManager
import com.example.fitnessmonitor.sensor.StepTrackerManager
import com.example.fitnessmonitor.ui.screens.DashboardScreen
import com.example.fitnessmonitor.ui.screens.GoalsScreen
import com.example.fitnessmonitor.ui.screens.HistoryScreen
import com.example.fitnessmonitor.ui.screens.ProfileScreen
import com.example.fitnessmonitor.ui.theme.FitnessMonitorTheme
import com.example.fitnessmonitor.util.NotificationHelper
import kotlinx.coroutines.delay

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("Dashboard", Icons.Default.Home)
    object Profile : Screen("Profile", Icons.Default.Person)
    object Goals : Screen("Goals", Icons.Default.Settings)
    object History : Screen("History", Icons.Default.DateRange)
}

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var dbHelper: FitnessDatabaseHelper
    private lateinit var stepTrackerManager: StepTrackerManager

    private var isActivityResumed by mutableStateOf(false)
    private var latestSteps = 0
    private var latestDurationSeconds = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)
        dbHelper = FitnessDatabaseHelper(this)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Load today's record or initialize
        val todayRecord = dbHelper.getTodayRecord()
        val initialSteps = todayRecord?.steps ?: 0
        val initialDuration = todayRecord?.durationSeconds ?: 0
        latestSteps = initialSteps
        latestDurationSeconds = initialDuration

        setContent {
            FitnessMonitorTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                var steps by remember { mutableStateOf(initialSteps) }
                var goal by remember { mutableStateOf(preferencesManager.getGoal()) }
                var durationSeconds by remember { mutableStateOf(initialDuration) }
                var goalAchievedNotified by remember { mutableStateOf(initialSteps >= preferencesManager.getGoal()) }

                // Timer for active duration
                LaunchedEffect(isActivityResumed) {
                    while (isActivityResumed) {
                        delay(1000L)
                        durationSeconds++
                        latestDurationSeconds = durationSeconds
                        // Save to SQLite periodically every 5 seconds while visible.
                        if (durationSeconds % 5 == 0) {
                            dbHelper.saveOrUpdateTodayRecord(steps, durationSeconds)
                        }
                    }
                }

                stepTrackerManager = remember {
                    StepTrackerManager(this@MainActivity) { newSteps ->
                        steps = newSteps
                        latestSteps = newSteps
                        dbHelper.saveOrUpdateTodayRecord(steps, durationSeconds)

                        // Check goal achievement and trigger notification
                        if (steps >= goal && !goalAchievedNotified) {
                            goalAchievedNotified = true
                            NotificationHelper.showGoalAchievedNotification(this@MainActivity, steps, goal)
                        }
                    }.apply {
                        setInitialSteps(initialSteps)
                    }
                }

                val items = listOf(
                    Screen.Dashboard,
                    Screen.Profile,
                    Screen.Goals,
                    Screen.History
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Dashboard -> {
                                DashboardScreen(
                                    steps = steps,
                                    goal = goal,
                                    durationSeconds = durationSeconds,
                                    isSensorAvailable = stepTrackerManager.isSensorAvailable,
                                    onSimulateStep = {
                                        stepTrackerManager.addManualStep(1)
                                    }
                                )
                            }
                            Screen.Profile -> {
                                ProfileScreen(preferencesManager = preferencesManager)
                            }
                            Screen.Goals -> {
                                GoalsScreen(
                                    preferencesManager = preferencesManager,
                                    onGoalUpdated = { newGoal ->
                                        goal = newGoal
                                        if (steps < newGoal) {
                                            // Re-arm notification when the user raises the target.
                                            goalAchievedNotified = false
                                        }
                                        if (steps >= goal && !goalAchievedNotified) {
                                            goalAchievedNotified = true
                                            NotificationHelper.showGoalAchievedNotification(this@MainActivity, steps, goal)
                                        }
                                    }
                                )
                            }
                            Screen.History -> {
                                HistoryScreen(dbHelper = dbHelper)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        if (::stepTrackerManager.isInitialized) {
            stepTrackerManager.startListening()
        }
    }

    override fun onPause() {
        isActivityResumed = false
        super.onPause()
        if (::stepTrackerManager.isInitialized) {
            stepTrackerManager.stopListening()
            dbHelper.saveOrUpdateTodayRecord(latestSteps, latestDurationSeconds)
        }
    }
}
