package com.example.airsenseco2_theapp.model

import android.bluetooth.BluetoothDevice

data class SavedDevice(
    val device: BluetoothDevice,
    val name: String
)