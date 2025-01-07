package com.example.airsenseco2_theapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import com.example.airsenseco2_theapp.R
import com.example.airsenseco2_theapp.ui.ConfirmationDialogue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.airsenseco2_theapp.ui.AppTitle

@Composable
fun WarmingUpScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
    val isWarmingUp by viewModel.isWarmingUp.collectAsState()
    val showDisconnectDialog by viewModel.showDisconnectDialog.collectAsState()

    if (showDisconnectDialog) {
        ConfirmationDialogue(
            title = "Disconnected",
            text = "Device disconnected. Reconnect?",
            confirmText = "YES",
            dismissText = "NO",
            onConfirm = {
                viewModel.resetDisconnectDialog()
                viewModel.autoConnectToSavedDevice(navController)
            },
            onDismiss = {
                viewModel.resetDisconnectDialog()
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
    }

    LaunchedEffect(isWarmingUp) {
        if (!isWarmingUp) {
            navController.navigate("training") {
                popUpTo("warmingup") { inclusive = true }
            }
        }
    }

    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        ) {
            AppTitle()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.airsense),
                contentDescription = "Warming Up",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )

            Text(
                text = "Waiting for device to warm up...",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.navigate("data") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text("View Previous Workouts While You Wait")
            }
        }
    }
}
