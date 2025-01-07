package com.example.airsenseco2_theapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.airsenseco2_theapp.model.SavedDevice
import com.example.airsenseco2_theapp.ui.AppTitle
import com.example.airsenseco2_theapp.ui.ConfirmationDialogue
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.airsenseco2_theapp.R

@Composable
fun BtConnectScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
    val isScanning by viewModel.isScanning.collectAsState()
    val devices by viewModel.foundDevices.collectAsState(initial = emptyList())
    val showDialog by viewModel.showDialog.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { destination ->
            if (destination == "home") {
                navController.navigate("home") {
                    popUpTo("btconnect") { inclusive = true }
                }
                viewModel.resetNavigation()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitle()

            if (!isScanning) {
                Image(
                    painter = painterResource(id = R.drawable.airsense),
                    contentDescription = "AirSenseCO2 Ready",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "Welcome to AirSenseCO2! \n" +
                        "To make your system complete, please start scanning, and connect to your AirSenseCO2-device.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(devices) { SavedDevice ->
                    DeviceItem(
                        deviceWithName = SavedDevice,
                        onClick = {
                            viewModel.selectDevice(SavedDevice.device)
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.toggleScan() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isScanning) "Stop scanning" else "Start scanning")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("bluetooth") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to HomeScreen")
            }
        }

        if (showDialog && selectedDevice != null) {
            ConfirmationDialogue(
                title = "Connect to device",
                text = "Would you like to connect to ${viewModel.getDeviceName(selectedDevice!!)}?",
                confirmText = "YES",
                dismissText = "NO",
                onConfirm = {
                    viewModel.confirmDeviceConnection(navController) },
                onDismiss = { viewModel.dismissDialog() }
            )
        }
    }
}

@Composable
fun DeviceItem(deviceWithName: SavedDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = deviceWithName.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "MAC: ${deviceWithName.device.address}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray)
        }
    }
}