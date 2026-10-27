package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adb.AdbManager
import com.example.discovery.DeviceDiscoveryService
import com.example.model.AdbCommandLog
import com.example.model.ConnectionState
import com.example.model.DeviceConnection
import com.example.model.DiscoveredDevice
import com.example.model.InstalledApp
import com.example.storage.RecentDevicesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppFilter {
    USER_ONLY,
    ALL,
    SYSTEM_ONLY
}

class AdbViewModel(application: Application) : AndroidViewModel(application) {

    private val adbManager = AdbManager(application)
    private val discoveryService = DeviceDiscoveryService(application)
    private val recentStore = RecentDevicesStore(application)

    val connectionState: StateFlow<ConnectionState> = adbManager.connectionState
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = discoveryService.discoveredDevices
    val isScanning: StateFlow<Boolean> = discoveryService.isScanning

    private val _recentDevices = MutableStateFlow<List<DeviceConnection>>(emptyList())
    val recentDevices: StateFlow<List<DeviceConnection>> = _recentDevices.asStateFlow()

    private val _commandLogs = MutableStateFlow<List<AdbCommandLog>>(emptyList())
    val commandLogs: StateFlow<List<AdbCommandLog>> = _commandLogs.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

    private val _showRemoteSheet = MutableStateFlow(false)
    val showRemoteSheet: StateFlow<Boolean> = _showRemoteSheet.asStateFlow()

    private val _appFilter = MutableStateFlow(AppFilter.USER_ONLY)
    val appFilter: StateFlow<AppFilter> = _appFilter.asStateFlow()

    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadRecentDevices()
        loadCommandHistory()
        // Start automatic Wi-Fi discovery on launch
        startDeviceDiscovery()
    }

    fun startDeviceDiscovery() {
        discoveryService.startDiscovery()
    }

    fun stopDeviceDiscovery() {
        discoveryService.stopDiscovery()
    }

    fun loadRecentDevices() {
        _recentDevices.value = recentStore.getRecentDevices()
    }

    fun loadCommandHistory() {
        _commandHistory.value = recentStore.getCommandHistory()
    }

    fun connect(ip: String, port: Int = 5555, label: String = "") {
        viewModelScope.launch {
            val result = adbManager.connect(ip, port)
            result.onSuccess { connectedState ->
                val deviceLabel = label.ifBlank { connectedState.deviceModel ?: "Android TV" }
                recentStore.saveDevice(ip, port, deviceLabel)
                loadRecentDevices()
                _statusMessage.value = "Connected to $deviceLabel"
                // Auto load user installed apps
                refreshApps()
            }.onFailure { error ->
                _statusMessage.value = error.message ?: "Connection failed"
            }
        }
    }

    fun disconnect() {
        adbManager.disconnect()
        _statusMessage.value = "Disconnected"
    }

    fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isBlank()) return
        recentStore.addCommandToHistory(trimmed)
        loadCommandHistory()

        viewModelScope.launch {
            val log = adbManager.executeShell(trimmed)
            _commandLogs.update { listOf(log) + it }
        }
    }

    fun sendKeyevent(keyCode: Int, name: String = "") {
        viewModelScope.launch {
            val success = adbManager.sendKeyevent(keyCode)
            if (!success && !adbManager.isConnected()) {
                _statusMessage.value = "Please connect to a device first"
            }
        }
    }

    fun sendText(text: String) {
        viewModelScope.launch {
            val success = adbManager.sendText(text)
            if (success) {
                _statusMessage.value = "Text sent to TV"
            }
        }
    }

    fun pingLatency() {
        viewModelScope.launch {
            val latency = adbManager.pingLatency()
            if (latency != null) {
                _statusMessage.value = "Ping: ${latency}ms"
            }
        }
    }

    fun refreshApps() {
        if (!adbManager.isConnected()) return
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = adbManager.getInstalledApps(includeSystem = true)
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun forceStopApp(packageName: String) {
        viewModelScope.launch {
            val success = adbManager.forceStopApp(packageName)
            _statusMessage.value = if (success) "Force stopped $packageName" else "Failed to stop $packageName"
        }
    }

    fun clearAppData(packageName: String) {
        viewModelScope.launch {
            val success = adbManager.clearAppData(packageName)
            _statusMessage.value = if (success) "Cleared data for $packageName" else "Failed to clear data"
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            val success = adbManager.launchApp(packageName)
            _statusMessage.value = if (success) "Launching $packageName on TV" else "Failed to launch app"
        }
    }

    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            val success = adbManager.uninstallApp(packageName)
            if (success) {
                _statusMessage.value = "Uninstalled $packageName"
                _installedApps.update { list -> list.filterNot { it.packageName == packageName } }
            } else {
                _statusMessage.value = "Failed to uninstall $packageName"
            }
        }
    }

    fun clearLogs() {
        _commandLogs.value = emptyList()
    }

    fun setRemoteSheetVisible(visible: Boolean) {
        _showRemoteSheet.value = visible
    }

    fun setAppFilter(filter: AppFilter) {
        _appFilter.value = filter
    }

    fun setAppSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    fun updateDeviceLabel(ip: String, port: Int, label: String) {
        recentStore.updateDeviceLabel(ip, port, label)
        loadRecentDevices()
    }

    fun removeDevice(ip: String, port: Int) {
        recentStore.removeDevice(ip, port)
        loadRecentDevices()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        discoveryService.stopDiscovery()
        adbManager.disconnect()
    }
}
