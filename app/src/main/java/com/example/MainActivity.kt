package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.ui.AdbViewModel
import com.example.ui.components.TvRemoteSheet
import com.example.ui.screens.AppManagerScreen
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.SettingsManagerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed

class MainActivity : ComponentActivity() {

    private val viewModel: AdbViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val connectionState by viewModel.connectionState.collectAsState()
                val discoveredDevices by viewModel.discoveredDevices.collectAsState()
                val isScanning by viewModel.isScanning.collectAsState()
                val recentDevices by viewModel.recentDevices.collectAsState()
                val commandLogs by viewModel.commandLogs.collectAsState()
                val commandHistory by viewModel.commandHistory.collectAsState()
                val installedApps by viewModel.installedApps.collectAsState()
                val isLoadingApps by viewModel.isLoadingApps.collectAsState()
                val showRemoteSheet by viewModel.showRemoteSheet.collectAsState()
                val appFilter by viewModel.appFilter.collectAsState()
                val appSearchQuery by viewModel.appSearchQuery.collectAsState()
                val statusMessage by viewModel.statusMessage.collectAsState()

                var selectedTab by remember { mutableIntStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "AdbTV Remote",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                }
                            },
                            actions = {
                                // Connection Status Pill
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when (connectionState) {
                                        is ConnectionState.Connected -> TerminalGreen.copy(alpha = 0.15f)
                                        is ConnectionState.Connecting -> MaterialTheme.colorScheme.primaryContainer
                                        is ConnectionState.Error -> TerminalRed.copy(alpha = 0.15f)
                                        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (connectionState) {
                                                        is ConnectionState.Connected -> TerminalGreen
                                                        is ConnectionState.Connecting -> MaterialTheme.colorScheme.primary
                                                        is ConnectionState.Error -> TerminalRed
                                                        is ConnectionState.Disconnected -> MaterialTheme.colorScheme.outline
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (connectionState) {
                                                is ConnectionState.Connected -> (connectionState as ConnectionState.Connected).deviceModel ?: "Connected"
                                                is ConnectionState.Connecting -> "Connecting..."
                                                is ConnectionState.Error -> "Error"
                                                is ConnectionState.Disconnected -> "Offline"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace,
                                            color = when (connectionState) {
                                                is ConnectionState.Connected -> TerminalGreen
                                                is ConnectionState.Connecting -> MaterialTheme.colorScheme.primary
                                                is ConnectionState.Error -> TerminalRed
                                                is ConnectionState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.setRemoteSheetVisible(true) },
                                    modifier = Modifier.testTag("topbar_remote_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SettingsRemote,
                                        contentDescription = "Open TV Remote",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Connections"
                                    )
                                },
                                label = { Text("Connection") },
                                modifier = Modifier.testTag("nav_connection_tab")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "Terminal"
                                    )
                                },
                                label = { Text("Terminal") },
                                modifier = Modifier.testTag("nav_terminal_tab")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "App Manager"
                                    )
                                },
                                label = { Text("TV Apps") },
                                modifier = Modifier.testTag("nav_apps_tab")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Settings & Tweaks"
                                    )
                                },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_settings_tab")
                            )
                        }
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.setRemoteSheetVisible(true) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SettingsRemote,
                                    contentDescription = null
                                )
                            },
                            text = { Text("TV Remote", fontWeight = FontWeight.SemiBold) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("floating_remote_fab")
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> ConnectionScreen(
                                connectionState = connectionState,
                                discoveredDevices = discoveredDevices,
                                isScanning = isScanning,
                                recentDevices = recentDevices,
                                onConnect = { ip, port, label -> viewModel.connect(ip, port, label) },
                                onDisconnect = { viewModel.disconnect() },
                                onStartScan = { viewModel.startDeviceDiscovery() },
                                onPing = { viewModel.pingLatency() },
                                onUpdateLabel = { ip, port, label -> viewModel.updateDeviceLabel(ip, port, label) },
                                onRemoveDevice = { ip, port -> viewModel.removeDevice(ip, port) }
                            )

                            1 -> TerminalScreen(
                                connectionState = connectionState,
                                commandLogs = commandLogs,
                                commandHistory = commandHistory,
                                onExecuteCommand = { cmd -> viewModel.executeCommand(cmd) },
                                onClearLogs = { viewModel.clearLogs() }
                            )

                            2 -> AppManagerScreen(
                                connectionState = connectionState,
                                installedApps = installedApps,
                                isLoading = isLoadingApps,
                                filter = appFilter,
                                searchQuery = appSearchQuery,
                                onFilterChange = { filter -> viewModel.setAppFilter(filter) },
                                onSearchChange = { q -> viewModel.setAppSearchQuery(q) },
                                onRefresh = { viewModel.refreshApps() },
                                onLaunchApp = { pkg -> viewModel.launchApp(pkg) },
                                onForceStopApp = { pkg -> viewModel.forceStopApp(pkg) },
                                onClearData = { pkg -> viewModel.clearAppData(pkg) },
                                onUninstallApp = { pkg -> viewModel.uninstallApp(pkg) },
                                onDisableApp = { pkg -> viewModel.disableApp(pkg) },
                                onEnableApp = { pkg -> viewModel.enableApp(pkg) },
                                onInstallApk = { uri -> viewModel.installApkFromUri(uri) },
                                onDownloadAndInstallApk = { url -> viewModel.downloadAndInstallApk(url) },
                                onSpeedBoost = { scale -> viewModel.setSpeedBoost(scale) },
                                onKillTasks = { viewModel.killBackgroundProcesses() }
                            )

                            3 -> SettingsManagerScreen(
                                connectionState = connectionState,
                                onExecuteCommand = { cmd -> viewModel.executeCommand(cmd) },
                                onSetSpeedBoost = { scale -> viewModel.setSpeedBoost(scale) },
                                onSetStayAwake = { stayAwake -> viewModel.setStayAwake(stayAwake) },
                                onSetDns = { dns -> viewModel.setPrivateDns(dns) },
                                onSetDensity = { dpi -> viewModel.setDensity(dpi) },
                                onResetDensity = { viewModel.resetDensity() }
                            )
                        }

                        // Floating Remote Control BottomSheet
                        if (showRemoteSheet) {
                            TvRemoteSheet(
                                connectionState = connectionState,
                                onKeyevent = { keyCode, name -> viewModel.sendKeyevent(keyCode, name) },
                                onSendText = { text -> viewModel.sendText(text) },
                                onDismiss = { viewModel.setRemoteSheetVisible(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
