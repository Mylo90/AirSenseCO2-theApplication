package com.example.airsenseco2_theapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.airsenseco2_theapp.ui.screens.BtConnectScreen
import com.example.airsenseco2_theapp.ui.screens.DataScreen
import com.example.airsenseco2_theapp.ui.screens.HomeScreen
import com.example.airsenseco2_theapp.ui.screens.WarmingUpScreen
import com.example.airsenseco2_theapp.ui.screens.WorkoutScreen
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel

@Composable
fun AppNavigation(navController: NavHostController, viewModel: BluetoothViewModel) {
    NavHost(navController, startDestination = "bluetooth") {
        composable("bluetooth") {
            BtConnectScreen(viewModel, navController)
        }
        composable("home") {
            HomeScreen(viewModel, navController)
        }
        composable("data") {
            DataScreen(viewModel, navController)
        }
        composable("training") {
            WorkoutScreen(viewModel, navController)
        }
        composable("warmingup") {
            WarmingUpScreen(viewModel, navController)
        }
    }
}