package com.example.airsenseco2_theapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.airsenseco2_theapp.viewmodel.BluetoothViewModel
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.navigation.compose.rememberNavController
import com.example.airsenseco2_theapp.ui.AppNavigation

class MainActivity : ComponentActivity() {
    private val viewModel: BluetoothViewModel by viewModels {
        BluetoothViewModel.provideFactory(application)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value == true }
        if (!granted) {
            Toast.makeText(this, "Bluetooth-Permission needed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBluetoothPermissions()

        setContent {
            val navController = rememberNavController()
            AppNavigation(navController, viewModel)

            LaunchedEffect(Unit) {
                viewModel.autoConnectToSavedDevice(navController)
            }
        }
    }

    private fun checkBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}