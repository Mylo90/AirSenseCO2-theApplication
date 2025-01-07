package com.example.airsenseco2_theapp.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.airsenseco2_theapp.model.BluetoothScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.airsenseco2_theapp.data.CsvHelper
import com.example.airsenseco2_theapp.data.DatabaseHelper
import com.example.airsenseco2_theapp.model.ConnectionState
import com.example.airsenseco2_theapp.model.TrainingSession
import kotlinx.coroutines.delay
import com.example.airsenseco2_theapp.model.Measurement
import com.example.airsenseco2_theapp.model.SavedDevice
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)
    private val csvHelper = CsvHelper(application)

    private val _hasToastBeenShown = MutableStateFlow(false)
    val hasToastBeenShown = _hasToastBeenShown.asStateFlow()

    private val _co2GraphData = MutableStateFlow<List<Pair<Int, Float>>>(emptyList())
    val co2GraphData = _co2GraphData.asStateFlow()

    private val _tempGraphData = MutableStateFlow<List<Pair<Int, Float>>>(emptyList())
    val tempGraphData = _tempGraphData.asStateFlow()

    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    private val _isTraining = MutableStateFlow(false)
    val isTraining = _isTraining.asStateFlow()

    private val _showConfirmation = MutableStateFlow(false)
    val showConfirmation = _showConfirmation.asStateFlow()

    private var totalElapsedTime: Long = 0
    private var lastPauseTime: Long = 0

    private val collectedData = mutableListOf<TrainingSession>()

    private var currentSession: MutableList<Measurement> = mutableListOf()
    private var startTime: Long = 0

    private val _isCollecting = MutableStateFlow(false)
    val isCollecting = _isCollecting.asStateFlow()


    private val bluetoothScanner = BluetoothScanner(application.applicationContext)
    private val _foundDevices = MutableStateFlow<List<SavedDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()
    //val sensorData = bluetoothScanner.sensorData // Lägg till detta för att observera inkommande data
    val co2Data = bluetoothScanner.co2Data
    val tempData = bluetoothScanner.tempData

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _selectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val selectedDevice = _selectedDevice.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    private val _savedSessions = MutableStateFlow<List<TrainingSession>>(emptyList())
    val savedSessions = _savedSessions.asStateFlow()

    private val _csvData = MutableStateFlow<List<String>>(emptyList())
    val csvData = _csvData.asStateFlow()

    private val _isWarmingUp = MutableStateFlow(true)
    val isWarmingUp = _isWarmingUp.asStateFlow()

    private val _showDisconnectDialog = MutableStateFlow(false)
    val showDisconnectDialog = _showDisconnectDialog.asStateFlow()

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome = _navigateToHome.asStateFlow()


    init {
        observeDevices()
        bluetoothScanner.autoConnectToSavedDevice()

        viewModelScope.launch {
            co2Data.collect { co2 ->
                if (co2 > 0 && _isWarmingUp.value) {
                    _isWarmingUp.value = false
                }
            }
        }
        viewModelScope.launch {
            bluetoothScanner.disconnectionEvent.collect { disconnected ->
                if (disconnected) {
                    _showDisconnectDialog.value = true  // Aktivera dialog
                    bluetoothScanner.resetDisconnectionEvent()  // Återställ händelsen
                }
            }
        }
        viewModelScope.launch {
            bluetoothScanner.deviceConnected.collect { connected ->
                if (connected) {
                    _showDisconnectDialog.value = false
                    val deviceName = bluetoothScanner.bluetoothGatt?.device?.let { getDeviceName(it) } ?: "Unknown Device"
                    Toast.makeText(
                        getApplication(),
                        "Connected to: $deviceName",
                        Toast.LENGTH_SHORT
                    ).show()
                    _connectionState.value = ConnectionState.CONNECTED
                } else {
                    Log.d("BluetoothViewModel", "Enhet frånkopplad.")
                }
            }
        }
    }
    fun toggleScan() {
        viewModelScope.launch {
            if (_isScanning.value) {
                stopScan()
            } else {
                startScan()
            }
            _isScanning.value = !_isScanning.value
        }
    }
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    suspend fun fetchSavedData() {
        val sessions = dbHelper.getAllSessions().map { session ->
            session.copy(
                formattedStartTime = formatTimestamp(session.startTime)  // Sätt det formaterade värdet
            )
        }
        _savedSessions.value = sessions
    }
    fun resetDisconnectDialog() {
        _showDisconnectDialog.value = false
    }

//    fun fetchCsvData() {
//        viewModelScope.launch {
//            val csv = csvHelper.readCsv()
//            _csvData.value = csv
//        }
//    }

    fun autoConnectToSavedDevice(navController: NavHostController) {
        viewModelScope.launch {
            val savedAddress = bluetoothScanner.getSavedDeviceAddress()
            if (savedAddress != null) {
                val device = bluetoothScanner.bluetoothAdapter?.getRemoteDevice(savedAddress)
                if (device != null) {
                    bluetoothScanner.connectToDevice(device)
                    _connectionState.value = ConnectionState.CONNECTED
                    _showDisconnectDialog.value = false


                    navController.navigate("home")
                    Log.d("BluetoothViewModel", "Ansluter automatiskt till: $savedAddress")
                } else {
                    _showDisconnectDialog.value = true
                    Log.e("BluetoothViewModel", "Ingen sparad enhet hittades, navigerar till Bluetooth-sökning.")
                    navController.navigate("bluetooth")
                }
            } else {
                _showDisconnectDialog.value = true
                Log.e("BluetoothViewModel", "Ingen enhet sparad, navigerar till Bluetooth-sökning.")
                navController.navigate("bluetooth")
            }
        }
    }

    private fun observeDevices() {
        viewModelScope.launch {
            bluetoothScanner.foundDevices.collect { devices ->
                val updatedDevices = devices.map { device ->
                    SavedDevice(
                        device = device,
                        name = getDeviceName(device)
                    )
                }
                _foundDevices.value = updatedDevices
            }
        }
    }

    fun getDeviceName(device: BluetoothDevice): String {
        return if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            device.name ?: "Unknown device"
        } else {
            "Permission needed"
        }
    }

    fun startScan() {
        viewModelScope.launch {
            bluetoothScanner.startScan()
        }
    }
    fun stopScan() {
        viewModelScope.launch {
            bluetoothScanner.stopScan()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothScanner.connectToDevice(device)
        _connectionState.value = ConnectionState.CONNECTED
        triggerNavigation("home")
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return BluetoothViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    fun exportToExcel(co2: Float) {
        viewModelScope.launch {
            csvHelper.exportToExcel(co2, tempData.value)
        }
    }

    fun startDataCollection(interval: Long = 1000L) {
        viewModelScope.launch {

            _isCollecting.value = true

            if (startTime == 0L) {
                startTime = System.currentTimeMillis()
            } else {
                startTime = System.currentTimeMillis()
            }

            while (_isCollecting.value) {
                val co2 = co2Data.value
                val temperature = tempData.value

                currentSession.add(
                    Measurement(
                        co2 = co2,
                        temperature = temperature,
                        timestamp = System.currentTimeMillis()
                    )
                )

                _timer.value = (((System.currentTimeMillis() - startTime) + totalElapsedTime) / 1000).toInt()
                _co2GraphData.value = _co2GraphData.value + (timer.value to co2)
                _tempGraphData.value = _tempGraphData.value + (timer.value to temperature)

                delay(interval)
            }
        }
    }
    fun saveTrainingSession() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val duration = totalElapsedTime

            val session = TrainingSession(
                measurements = currentSession,
                startTime = startTime,
                duration = duration
            )
            dbHelper.insertTrainingSession(session)
            csvHelper.writeTrainingSessionToCsv(session)
            resetTrainingSession()
        }
    }
    fun clearCurrentSession() {
        currentSession.clear()
    }
    fun pauseDataCollection() {
        if (_isCollecting.value) {
            _isCollecting.value = false
            lastPauseTime = System.currentTimeMillis()
            totalElapsedTime += lastPauseTime - startTime
        }
    }
    fun resetTrainingSession() {
        viewModelScope.launch {
            _timer.value = 0
            _co2GraphData.value = emptyList()
            _tempGraphData.value = emptyList()
            currentSession.clear()
            startTime = 0L
            totalElapsedTime = 0L
        }
    }
    fun selectDevice(device: BluetoothDevice) {
        _selectedDevice.value = device
        _showDialog.value = true
    }

    fun confirmDeviceConnection(navController: NavHostController) {
        _selectedDevice.value?.let { device ->
            bluetoothScanner.connectToDevice(device)
            _showDialog.value = false

            viewModelScope.launch {
                bluetoothScanner.deviceConnected.collect { connected ->
                    if (connected) {
                        val deviceName = getDeviceName(device)
                        Toast.makeText(
                            getApplication(),
                            "Connected to: $deviceName",
                            Toast.LENGTH_SHORT
                        ).show()
                        _connectionState.value = ConnectionState.CONNECTED
                        navController.navigate("home") {
                            popUpTo("btconnect") { inclusive = true }
                        }
                        this.cancel()
                    }
                }
            }
        }
    }

    fun dismissDialog() {
        _showDialog.value = false
    }
    fun triggerNavigation(destination: String) {
        _navigationEvent.value = destination
    }

    fun resetNavigation() {
        _navigationEvent.value = null
    }
    fun handleDisconnectDialogResponse(save: Boolean) {
        viewModelScope.launch {
            if (save) {
                saveTrainingSession()
            } else {
                clearCurrentSession()
            }
            _showDisconnectDialog.value = false
        }
    }

}