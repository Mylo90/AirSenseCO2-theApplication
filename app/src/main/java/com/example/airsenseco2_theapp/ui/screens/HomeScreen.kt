package com.example.airsenseco2_theapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import com.example.airsenseco2_theapp.R
import androidx.compose.ui.text.style.TextAlign
import com.example.airsenseco2_theapp.ui.AppTitle
import com.example.airsenseco2_theapp.ui.ConfirmationDialogue

@Composable
fun HomeScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
    val co2Data by viewModel.co2Data.collectAsState()
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
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Placera AppTitle högst upp på skärmen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        ) {
            AppTitle()
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(96.dp))

            Image(
                painter = painterResource(id = R.drawable.airsense),
                contentDescription = "AirSenseCO2 Ready",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Welcome to AirSenseCO2! \n" +
                        "Track your respiratory function in real-time with continuous CO2 monitoring. " +
                        "Simply put on your AirSenseCO2-mask and start your workout!  ",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = {
                    navController.navigate("data") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Previous Workouts")
                }
                Button(onClick = {
                    if (co2Data > 0) {
                        navController.navigate("training")
                    } else {
                        navController.navigate("warmingup")
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("New Workout!")
                }
            }

            Button(
                onClick = {
                    viewModel.autoConnectToSavedDevice(navController)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Reconnect to Device")
            }
        }
    }
}
