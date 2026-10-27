package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.DeviceConnection
import com.example.model.DiscoveredDevice
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConnectionScreen(
    connectionState: ConnectionState,
    discoveredDevices: List<DiscoveredDevice>,
    isScanning: Boolean,
    recentDevices: List<DeviceConnection>,
    onConnect: (String, Int, String) -> Unit,
    onDisconnect: () -> Unit,
    onStartScan: () -> Unit,
    onPing: () -> Unit,
    onUpdateLabel: (String, Int, String) -> Unit,
    onRemoveDevice: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var ipInput by remember { mutableStateOf("") }
    var portInput by remember { mutableStateOf("5555") }
    var editingDevice by remember { mutableStateOf<DeviceConnection?>(null) }
    var newLabelInput by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("connection_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Active Connection Status Card
            ConnectionStatusBanner(
                connectionState = connectionState,
                onDisconnect = onDisconnect,
                onPing = onPing
            )
        }

        item {
            // Quick Connect Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_connect_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Quick Connect",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Setup Help",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text("Target IP Address") },
                            placeholder = { Text("192.168.1.100") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .weight(2.5f)
                                .testTag("ip_input_field"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Port") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                val port = portInput.toIntOrNull() ?: 5555
                                if (ipInput.isNotBlank()) {
                                    onConnect(ipInput, port, "")
                                }
                            }),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("port_input_field"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val port = portInput.toIntOrNull() ?: 5555
                            if (ipInput.isNotBlank()) {
                                onConnect(ipInput, port, "")
                            }
                        },
                        enabled = ipInput.isNotBlank() && connectionState !is ConnectionState.Connecting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("connect_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (connectionState is ConnectionState.Connecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Connecting...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CastConnected,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect via Wireless ADB", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Discovered Devices Section (Auto Search)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discovered Android TVs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (discoveredDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${discoveredDevices.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onStartScan,
                    enabled = !isScanning,
                    modifier = Modifier.testTag("scan_refresh_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scanning...", fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Wi-Fi", fontSize = 13.sp)
                    }
                }
            }
        }

        if (discoveredDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isScanning) "Scanning local Wi-Fi for Android TVs..." else "No Android TVs discovered yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Make sure Wireless Debugging is on (port 5555)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(discoveredDevices) { device ->
                DiscoveredDeviceCard(
                    device = device,
                    onConnect = { onConnect(device.ip, device.port, device.name) }
                )
            }
        }

        // Recent Saved Connections
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recent Connections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (recentDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connected devices will be saved here for instant 1-tap reconnect",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentDevices) { device ->
                RecentDeviceCard(
                    device = device,
                    onConnect = { onConnect(device.ip, device.port, device.label) },
                    onEdit = {
                        editingDevice = device
                        newLabelInput = device.label
                    },
                    onDelete = { onRemoveDevice(device.ip, device.port) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Label Edit Dialog
    if (editingDevice != null) {
        val dev = editingDevice!!
        AlertDialog(
            onDismissRequest = { editingDevice = null },
            title = { Text("Rename TV Device") },
            text = {
                OutlinedTextField(
                    value = newLabelInput,
                    onValueChange = { newLabelInput = it },
                    label = { Text("Device Label / Nickname") },
                    placeholder = { Text("e.g. Living Room Sony TV") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateLabel(dev.ip, dev.port, newLabelInput)
                    editingDevice = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDevice = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Wireless Debugging Guide Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("How to Enable Wireless ADB on Android TV") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. On your Android TV, open Settings → Device Preferences → About.", style = MaterialTheme.typography.bodyMedium)
                    Text("2. Scroll down to 'Build' and click it 7 times until you see 'You are now a developer!'.", style = MaterialTheme.typography.bodyMedium)
                    Text("3. Go back to Developer Options, turn on 'USB Debugging' and 'Wireless Debugging'.", style = MaterialTheme.typography.bodyMedium)
                    Text("4. Note your TV's Wi-Fi IP address and default port (5555).", style = MaterialTheme.typography.bodyMedium)
                    Text("5. When connecting, check your TV screen and select 'Always allow from this computer'.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun ConnectionStatusBanner(
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    onPing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("connection_status_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ConnectionState.Connected -> TerminalGreen.copy(alpha = 0.12f)
                is ConnectionState.Connecting -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                is ConnectionState.Error -> TerminalRed.copy(alpha = 0.12f)
                is ConnectionState.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            when (connectionState) {
                is ConnectionState.Connected -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(TerminalGreen)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Connected to TV",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalGreen
                                )
                                Text(
                                    text = connectionState.deviceModel ?: "Android TV Device",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDisconnect,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalRed)
                        ) {
                            Text("Disconnect", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${connectionState.ip}:${connectionState.port}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${connectionState.latencyMs}ms",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onPing, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Ping test",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                is ConnectionState.Connecting -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Connecting to TV...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${connectionState.ip}:${connectionState.port}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                is ConnectionState.Error -> {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = TerminalRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Connection Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TerminalRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = connectionState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is ConnectionState.Disconnected -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Wireless ADB Disconnected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: DiscoveredDevice,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovered_device_${device.ip}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${device.ip}:${device.port}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Found via ${device.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = onConnect,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Connect", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RecentDeviceCard(
    device: DeviceConnection,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_device_${device.ip}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.label.ifBlank { "Android TV" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "${device.ip}:${device.port}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last seen: ${dateFormat.format(Date(device.lastConnectedTimestamp))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Connect", fontSize = 12.sp)
                }
            }
        }
    }
}
