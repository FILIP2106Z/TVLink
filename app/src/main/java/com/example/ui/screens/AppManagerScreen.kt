package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.InstalledApp
import com.example.ui.AppFilter
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalRed

@Composable
fun AppManagerScreen(
    connectionState: ConnectionState,
    installedApps: List<InstalledApp>,
    isLoading: Boolean,
    filter: AppFilter,
    searchQuery: String,
    onFilterChange: (AppFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onForceStopApp: (String) -> Unit,
    onClearData: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is ConnectionState.Connected
    var appToConfirmClear by remember { mutableStateOf<InstalledApp?>(null) }
    var appToConfirmUninstall by remember { mutableStateOf<InstalledApp?>(null) }

    val filteredApps = remember(installedApps, filter, searchQuery) {
        installedApps.filter { app ->
            val matchesFilter = when (filter) {
                AppFilter.USER_ONLY -> !app.isSystem
                AppFilter.SYSTEM_ONLY -> app.isSystem
                AppFilter.ALL -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                app.packageName.contains(searchQuery, ignoreCase = true) ||
                app.label.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("app_manager_screen")
    ) {
        // Search & Refresh Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search installed packages...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("app_search_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRefresh,
                enabled = isConnected && !isLoading,
                modifier = Modifier.size(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Apps",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == AppFilter.USER_ONLY,
                onClick = { onFilterChange(AppFilter.USER_ONLY) },
                label = { Text("User Apps") },
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = filter == AppFilter.ALL,
                onClick = { onFilterChange(AppFilter.ALL) },
                label = { Text("All Apps") },
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = filter == AppFilter.SYSTEM_ONLY,
                onClick = { onFilterChange(AppFilter.SYSTEM_ONLY) },
                label = { Text("System Apps") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (!isConnected) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Connect to Android TV first",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Once connected, your TV's installed apps will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isLoading && installedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching installed packages via ADB...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No packages matching \"$searchQuery\"" else "No apps found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        onLaunch = { onLaunchApp(app.packageName) },
                        onForceStop = { onForceStopApp(app.packageName) },
                        onClearData = { appToConfirmClear = app },
                        onUninstall = { appToConfirmUninstall = app }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Confirmation Dialog: Clear Data
    if (appToConfirmClear != null) {
        val app = appToConfirmClear!!
        AlertDialog(
            onDismissRequest = { appToConfirmClear = null },
            icon = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = TerminalAmber) },
            title = { Text("Clear App Data?") },
            text = {
                Text("This will delete all temporary data, cache, and sign-ins for ${app.packageName} on your TV.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData(app.packageName)
                        appToConfirmClear = null
                    }
                ) {
                    Text("Clear Data", color = TerminalAmber)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToConfirmClear = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Uninstall
    if (appToConfirmUninstall != null) {
        val app = appToConfirmUninstall!!
        AlertDialog(
            onDismissRequest = { appToConfirmUninstall = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = TerminalRed) },
            title = { Text("Uninstall App?") },
            text = {
                Text("Are you sure you want to remotely uninstall ${app.packageName} from your Android TV?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUninstallApp(app.packageName)
                        appToConfirmUninstall = null
                    }
                ) {
                    Text("Uninstall", color = TerminalRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToConfirmUninstall = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AppItemCard(
    app: InstalledApp,
    onLaunch: () -> Unit,
    onForceStop: () -> Unit,
    onClearData: () -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (app.isSystem) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.label.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (app.isSystem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (app.isSystem) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = if (app.isSystem) "System" else "User",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                color = if (app.isSystem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Text(
                        text = app.packageName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick actions on each app
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onForceStop,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Force Stop",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onClearData,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear Data",
                        tint = TerminalAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (!app.isSystem) {
                    IconButton(
                        onClick = onUninstall,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Uninstall",
                            tint = TerminalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
