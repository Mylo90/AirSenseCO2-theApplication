package com.example.airsenseco2_theapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.ui.AppTitle
import com.example.airsenseco2_theapp.ui.ConfirmationDialogue
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import com.example.airsenseco2_theapp.model.TrainingSession


@Composable
fun DataScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
    val sessions by viewModel.savedSessions.collectAsState()
    val csvData by viewModel.csvData.collectAsState()
    val showDisconnectDialog by viewModel.showDisconnectDialog.collectAsState()

    if (showDisconnectDialog) {
        ConfirmationDialogue(
            title = "Disconnected",
            text = "The device is disconnected. Would you like to reconnect?",
            confirmText = "YES",
            dismissText = "NO",
            onConfirm = {
                viewModel.resetDisconnectDialog()
                viewModel.autoConnectToSavedDevice(navController)
            },
            onDismiss = {
                viewModel.resetDisconnectDialog()
                navController.navigate("home")
            }
        )
    }
    LaunchedEffect(Unit) {
        viewModel.fetchSavedData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AppTitle()

        Text("Previous Workouts!", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)

        LazyColumn {
            items(sessions) { session ->
                ExpandableWorkoutItem(session)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

//        LazyColumn {
//            items(sessions) { session ->
//                Text("Workout ID: ${session.id}")
//                Text("Start time: ${(session.formattedStartTime)}")
//                Text("Duration: ${session.duration / 1000} seconds")
//
//                session.measurements.forEach { measurement ->
//                    Text("CO2: ${measurement.co2} PPM, Temp: ${measurement.temperature}°C")
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }


    }

}
@Composable
fun ExpandableWorkoutItem(session: TrainingSession) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) Color.LightGray else Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workout ID: ${session.id}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "Start time: ${session.formattedStartTime}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                session.measurements.forEach { measurement ->
                    Text(
                        text = "CO2: ${measurement.co2} PPM, Temp: ${measurement.temperature}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Duration: ${session.duration / 1000} seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}