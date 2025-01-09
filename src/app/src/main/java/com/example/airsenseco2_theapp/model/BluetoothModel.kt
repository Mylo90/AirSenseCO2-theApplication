package com.example.airsenseco2_theapp.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class BluetoothScanner(private val context: Context) {
    val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    var bluetoothGatt: BluetoothGatt? = null

    private val sharedPreferences = context.getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE)

    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()

    private val _co2Data = MutableStateFlow(0f)
    val co2Data = _co2Data.asStateFlow()

    private val _tempData = MutableStateFlow(0f)
    val tempData = _tempData.asStateFlow()

    private val scanPeriod: Long = 10000

    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected = _deviceConnected.asStateFlow()


    private val arduinoServiceUuid = UUID.fromString("0000180C-0000-1000-8000-00805f9b34fb") // Matchar Arduino-tjänst
    private val co2CharacteristicUuid = UUID.fromString("00002A6E-0000-1000-8000-00805f9b34fb")
    private val tempCharacteristicUuid = UUID.fromString("00002A6F-0000-1000-8000-00805f9b34fb")

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _disconnectionEvent = MutableStateFlow(false)
    val disconnectionEvent = _disconnectionEvent.asStateFlow()


    // Callback för att hantera resultat från skanningen
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result.device)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { result ->
                handleScanResult(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothScanner", "Skanning misslyckades: Felkod $errorCode")
        }
    }
    val filters = listOf(
        ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(arduinoServiceUuid))
            .build()
    )

    private fun saveDeviceAddress(address: String) {
        sharedPreferences.edit().putString("DEVICE_ADDRESS", address).apply()
    }

    fun getSavedDeviceAddress(): String? {
        return sharedPreferences.getString("DEVICE_ADDRESS", null)
    }

    private fun handleScanResult(device: BluetoothDevice) {
        try {
            if (_foundDevices.value.none { it.address == device.address }) {
                _foundDevices.value += device
                Log.d("BluetoothScanner", "Hittade enhet: ${device.name ?: "Okänd"} - ${device.address}")
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothScanner", "Behörighet nekades: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermissions()) {
            Log.e("BluetoothScanner", "Behörigheter saknas för Bluetooth-skanning")
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BluetoothScanner", "Bluetooth är avstängt eller ej tillgängligt.")
            return
        }
        if (bluetoothLeScanner == null) {
            Log.e("BluetoothScanner", "BluetoothLeScanner är null.")
            return
        }
        bluetoothGatt?.let {
            Log.d("BluetoothScanner", "Stänger tidigare GATT-anslutning innan skanning.")
            it.disconnect()
            it.close()
            bluetoothGatt = null
        }
        _foundDevices.value = emptyList()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner.startScan(filters, settings, scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            stopScan()
        }, scanPeriod)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        Log.d("BluetoothScanner", "Skanning stoppad.")
    }

    @SuppressLint("MissingPermission")
    fun autoConnectToSavedDevice() {
        val savedAddress = getSavedDeviceAddress()
        if (savedAddress != null) {
            val device = bluetoothAdapter?.getRemoteDevice(savedAddress)
            if (device != null) {
                connectToDevice(device)
                Log.d("BluetoothScanner", "Försöker automatiskt ansluta till: $savedAddress")
            } else {
                Log.e("BluetoothScanner", "Ingen sparad enhet hittades")
            }
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        if (!hasPermissions()) {
            Log.e("BluetoothScanner", "Behörighet saknas för att ansluta.")
            return
        }

        stopScan()

        bluetoothGatt?.let {
            Log.d("BluetoothScanner", "Stänger tidigare GATT-anslutning.")
            it.disconnect()
            it.close()
            bluetoothGatt = null
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        saveDeviceAddress(device.address)
        Log.d("BluetoothScanner", "Försöker ansluta till: ${device.name ?: "Okänd"}")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BluetoothScanner", "Ansluten till GATT-server.")
                    try {
                        _connectionState.value = ConnectionState.CONNECTED
                        _deviceConnected.value = true
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e("BluetoothScanner", "Behörighet saknas vid discoverServices: ${e.message}")
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BluetoothScanner", "Frånkopplad från enheten.")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _deviceConnected.value = false
                    _disconnectionEvent.value = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            gatt.close()
                            bluetoothGatt = null
                        } catch (e: SecurityException) {
                            Log.e("BluetoothScanner", "Behörighet saknas vid close: ${e.message}")
                        }
                    }, 500)
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(arduinoServiceUuid)
                if (service != null) {
                    Log.d("BluetoothScanner", "Tjänst hittad: $arduinoServiceUuid")

                    val co2Characteristic = service.getCharacteristic(co2CharacteristicUuid)
                    val tempCharacteristic = service.getCharacteristic(tempCharacteristicUuid)

                    if (co2Characteristic != null) {
                        Log.d("BluetoothScanner", "CO2 Characteristic hittad")
                        enableNotifications(co2Characteristic)
                        Handler(Looper.getMainLooper()).postDelayed({
                            enableNotifications(tempCharacteristic)
                        }, 1000)
                    } else {
                        Log.e("BluetoothScanner", "CO2 Characteristic saknas")
                    }

                    if (tempCharacteristic != null) {
                        Log.d("BluetoothScanner", "Temp Characteristic hittad: ${tempCharacteristicUuid}")
                        Handler(Looper.getMainLooper()).postDelayed({
                            enableNotifications(tempCharacteristic)
                        }, 1000)
                    } else {
                        Log.e("BluetoothScanner", "Temp Characteristic saknas")
                    }
                } else {
                    Log.e("BluetoothScanner", "Ingen tjänst upptäckt")
                }
            } else {
                try {
                    gatt.close()
                    bluetoothGatt = null
                } catch (e: SecurityException) {
                    Log.e("BluetoothScanner", "Misslyckades att upptäcka tjänster.")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val data = String(value, Charsets.UTF_8)
            Log.d("BluetoothScanner", "Inkommande data: $data från UUID: ${characteristic.uuid}")

            val co2Regex = Regex("CO2:\\s*(-?[0-9]+\\.?[0-9]*)\\s*PPM")
            val tempRegex = Regex("Temp:\\s*(-?[0-9]+\\.?[0-9]*)\\s*C")

            when (characteristic.uuid) {
                co2CharacteristicUuid -> {
                    co2Regex.find(data)?.groups?.get(1)?.value?.toFloatOrNull()?.let {
                        // only for testing
                        //val positiveValue = kotlin.math.abs(it)
                        //Log.d("BluetoothScanner", "Konverterad CO2-data: $positiveValue")
                        //_co2Data.value = positiveValue
                         _co2Data.value = it
                        Log.d("BluetoothScanner", "Uppdaterad CO2-data: $it")
                    } ?: Log.e("BluetoothScanner", "Misslyckades att matcha CO2-data")
                }
                tempCharacteristicUuid -> {
                    tempRegex.find(data)?.groups?.get(1)?.value?.toFloatOrNull()?.let {
                        // only for testing
                        //val positiveValue = kotlin.math.abs(it)
                        //Log.d("BluetoothScanner", "Konverterad Temp-data: $positiveValue")
                        //_tempData.value = positiveValue
                        _tempData.value = it
                        Log.d("BluetoothScanner", "Uppdaterad Temp-data: $it")
                    } ?: Log.e("BluetoothScanner", "Misslyckades att tolka temperatur")
                }
                else -> {
                    Log.d("BluetoothScanner", "Okänd UUID: ${characteristic.uuid} med data: $data")
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothScanner", "Notifikationer aktiverade för UUID: ${descriptor.characteristic.uuid}")
            } else {
                Log.e("BluetoothScanner", "Misslyckades att aktivera notifikationer för UUID: ${descriptor.characteristic.uuid}, status: $status")
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        )
        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            val success = bluetoothGatt?.writeDescriptor(descriptor)
            if (success == true) {
                Log.d("BluetoothScanner", "Försöker aktivera notifikationer för ${characteristic.uuid}")
            } else {
                Log.e("BluetoothScanner", "Misslyckades att skriva descriptor för ${characteristic.uuid}")
            }
        } else {
            Log.e("BluetoothScanner", "Descriptor saknas för ${characteristic.uuid}")
        }
    }
    fun resetDisconnectionEvent() {
        _disconnectionEvent.value = false
    }
}

