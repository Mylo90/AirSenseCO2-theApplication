//package com.example.airsenseco2_theapp.ui.screens
//
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
//import com.example.airsenseco2_theapp.R
//import androidx.compose.ui.text.style.TextAlign
//import com.example.airsenseco2_theapp.ui.ConfirmationDialogue
//
//@Composable
//fun HomeScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
//    val co2Data by viewModel.co2Data.collectAsState()
//    //val tempData by viewModel.tempData.collectAsState()
//
//    val showDisconnectDialog by viewModel.showDisconnectDialog.collectAsState()
//
//    if (showDisconnectDialog) {
//        ConfirmationDialogue(
//            title = "Disconnected",
//            text = "The device is disconnected. Would you like to reconnect?",
//            confirmText = "YES",
//            dismissText = "NO",
//            onConfirm = {
//                viewModel.resetDisconnectDialog()
//                viewModel.autoConnectToSavedDevice(navController)
//            },
//            onDismiss = {
//                viewModel.resetDisconnectDialog()
//                navController.navigate("home") {
//                    popUpTo("home") { inclusive = true }
//                }
//                viewModel.resetNavigation()
//            }
//        )
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "AirSenseCO2  -the App",
//            fontSize = 48.sp,
//            color = Color.Gray,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 32.dp, bottom = 16.dp),
//            textAlign = TextAlign.Center
//        )
//        ReadyScreen(viewModel, navController, co2Data)
//    }
//}
//@Composable
//fun WarmingUpScreen(viewModel: BluetoothViewModel, navController: NavHostController) {
//    val isWarmingUp by viewModel.isWarmingUp.collectAsState()
//    val showDisconnectDialog by viewModel.showDisconnectDialog.collectAsState()
//    if (showDisconnectDialog) {
//        ConfirmationDialogue(
//            title = "Disconnected",
//            text = "The device was disconnected. Would you like to reconnect?",
//            confirmText = "YES",
//            dismissText = "NO",
//            onConfirm = {
//                viewModel.resetDisconnectDialog()
//                viewModel.autoConnectToSavedDevice(navController)
//            },
//            onDismiss = {
//                viewModel.resetDisconnectDialog()
//                navController.navigate("home") {
//                    popUpTo("home") { inclusive = true }
//                }
//                viewModel.resetNavigation()
//            }
//        )
//    }
//    LaunchedEffect(isWarmingUp) {
//        if (!isWarmingUp) {
//            navController.navigate("training") {
//                popUpTo("warmingup") { inclusive = true }
//            }
//        }
//    }
//    val infiniteTransition = rememberInfiniteTransition()
//    val scale by infiniteTransition.animateFloat(
//        initialValue = 0.9f,
//        targetValue = 1.1f,
//        animationSpec = infiniteRepeatable(
//            tween(1000, easing = LinearEasing),
//            RepeatMode.Reverse
//        )
//    )
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.airsense),
//            contentDescription = "AirSenseCO2 Ready",
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .graphicsLayer(
//                    scaleX = scale,
//                    scaleY = scale
//                )
//                .padding(bottom = 24.dp),
//            contentScale = ContentScale.Fit
//        )
//        Text(
//            text = "Waiting for AirSenseCO2 to warm up...",
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.padding(top = 24.dp)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            navController.navigate("data") },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Gray,
//                contentColor = Color.White
//            )
//        ) {
//            Text("View Previous Workouts While You Wait")
//        }
//    }
//}
//
//@Composable
//fun ReadyScreen(viewModel: BluetoothViewModel, navController: NavHostController, co2Data: Float) {
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.airsense),
//            contentDescription = "AirSenseCO2 Ready",
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .padding(bottom = 24.dp),
//            contentScale = ContentScale.Fit
//        )
//        Text(
//            text = "Welcome to AirSenseCO2! \n" +
//                    "Track your respiratory function in real-time with continuous CO2 monitoring. " +
//                    "Simply put on your AirSenseCO2-mask and start your workout!  ",
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
//        )
//
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Button(onClick = {
//                navController.navigate("data") },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Gray,
//                    contentColor = Color.White
//                )
//            ) {
//                Text("Previous Workouts")
//            }
//            Button(onClick = {
//                if (co2Data > 0) {
//                    navController.navigate("training")  // Navigera direkt till WorkoutScreen
//                } else {
//                    navController.navigate("warmingup")  // Navigera till WarmingUpScreen om CO2 är 0
//                }
//            },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Gray,
//                    contentColor = Color.White
//                )
//            ) {
//                Text("New Workout!")
//            }
//        }
//        Button(
//            onClick = {
//                viewModel.autoConnectToSavedDevice(navController)
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Gray,
//                contentColor = Color.White
//            ),
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Text("Reconnect to Device")
//        }
//    }
//}