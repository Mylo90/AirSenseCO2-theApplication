package com.example.airsenseco2_theapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.ui.AppTitle
import com.example.airsenseco2_theapp.ui.ConfirmationDialogue
import com.example.airsenseco2_theapp.ui.Graphs

@Composable
fun WorkoutScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
    val co2GraphData by viewModel.co2GraphData.collectAsState()
    //val tempGraphData by viewModel.tempGraphData.collectAsState()
    val timer by viewModel.timer.collectAsState()

    var isTraining by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    val showDisconnectDialog by viewModel.showDisconnectDialog.collectAsState()
    var showReconnectDialog by remember { mutableStateOf(false) }
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { destination ->
            navController.navigate(destination) {
                popUpTo("training") { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)  // Lägger till utrymme från status bar
        ) {
            AppTitle()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "CO2 (PPM)",
                fontSize = 32.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Graphs(co2GraphData)

            // Temperatur-graf
//            Text("Temp (°C)", style = MaterialTheme.typography.bodyLarge)
//            Graph(tempGraphData)

            Text(
                text = "Time: ${timer}s",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    isTraining = true
                    viewModel.startDataCollection() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Start")
                }
                Button(onClick = {
                    isTraining = false
                    viewModel.pauseDataCollection() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Stop")
                }
                Button(onClick = {
                    isTraining = false
//                    viewModel.stopDataCollection()
                    showConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
                Button(onClick = {
                    isTraining = false
                    viewModel.resetTrainingSession() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear")
                }
            }
        }

        if (showConfirmation) {
            ConfirmationDialogue(
                title = "Save workout",
                text = "Would you like to save?",
                confirmText = "YES",
                dismissText = "NO",
                onConfirm = {
                    viewModel.saveTrainingSession()
                    showConfirmation = false
                },
                onDismiss = {
                    showConfirmation = false
                    viewModel.clearCurrentSession()
                }
            )
        }
        if (showDisconnectDialog) {
            ConfirmationDialogue(
                title = "Device disconnected",
                text = "The device was disconnected. Do you want to save your workout?",
                confirmText = "YES",
                dismissText = "NO",
                onConfirm = {
                    isTraining = false
                    viewModel.handleDisconnectDialogResponse(true)
                    showReconnectDialog = true
                },
                onDismiss = {
                    viewModel.handleDisconnectDialogResponse(false)
                    showReconnectDialog = true
                }
            )
        }
        if (showReconnectDialog) {
            ConfirmationDialogue(
                title = "Device Disconnected",
                text = "Would you like to reconnect?",
                confirmText = "YES",
                dismissText = "NO",
                onConfirm = {
                    viewModel.autoConnectToSavedDevice(navController)
                    showReconnectDialog = false
                },
                onDismiss = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                    showReconnectDialog = false
                }
            )
        }
    }
}