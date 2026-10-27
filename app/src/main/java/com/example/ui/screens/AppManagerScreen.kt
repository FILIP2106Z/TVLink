package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.InstalledApp
import com.example.ui.AppFilter
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen
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
    onDisableApp: (String) -> Unit,
    onEnableApp: (String) -> Unit,
    onInstallApk: (Uri) -> Unit,
    onDownloadAndInstallApk: (String) -> Unit,
    onSpeedBoost: (Float) -> Unit,
    onKillTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is ConnectionState.Connected
    var appToConfirmClear by remember { mutableStateOf<InstalledApp?>(null) }
    var appToConfirmUninstall by remember { mutableStateOf<InstalledApp?>(null) }
    var appToConfirmDisable by remember { mutableStateOf<InstalledApp?>(null) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showUrlSideloadDialog by remember { mutableStateOf(false) }
    var customApkUrl by remember { mutableStateOf("") }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onInstallApk(it) }
    }

    val filteredApps = remember(installedApps, filter, searchQuery) {
        installedApps.filter { app ->
            val matchesFilter = when (filter) {
                AppFilter.USER_ONLY -> !app.isSystem && !app.isDisabled
                AppFilter.SYSTEM_ONLY -> app.isSystem && !app.isDisabled
                AppFilter.DISABLED_ONLY -> app.isDisabled
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
        // Quick Action Bar: Local APK, GitHub/URL Sideload, TV Speed Booster
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { apkPickerLauncher.launch("application/vnd.android.package-archive") },
                enabled = isConnected,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("sideload_apk_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("File APK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            ElevatedButton(
                onClick = { showUrlSideloadDialog = true },
                enabled = isConnected,
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("url_sideload_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GitHub / Web APK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { showSpeedDialog = true },
                enabled = isConnected,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("speed_boost_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Booster", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

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
                placeholder = { Text("Search packages...") },
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

        // Filter Chips (Horizontal Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
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
                label = { Text("All (${installedApps.size})") },
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = filter == AppFilter.SYSTEM_ONLY,
                onClick = { onFilterChange(AppFilter.SYSTEM_ONLY) },
                label = { Text("System") },
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = filter == AppFilter.DISABLED_ONLY,
                onClick = { onFilterChange(AppFilter.DISABLED_ONLY) },
                label = { Text("Debloated (${installedApps.count { it.isDisabled }})") },
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
                        text = "Once connected, your TV's installed packages will appear here",
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
                        onUninstall = { appToConfirmUninstall = app },
                        onDisable = { appToConfirmDisable = app },
                        onEnable = { onEnableApp(app.packageName) }
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
                Text("This will wipe all cached data, configuration, and logins for ${app.packageName} on your Android TV.")
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
                Text("Are you sure you want to completely uninstall ${app.packageName} from your Android TV?")
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

    // Confirmation Dialog: Disable / Debloat
    if (appToConfirmDisable != null) {
        val app = appToConfirmDisable!!
        AlertDialog(
            onDismissRequest = { appToConfirmDisable = null },
            icon = { Icon(Icons.Default.Block, contentDescription = null, tint = TerminalAmber) },
            title = { Text("Debloat / Disable App?") },
            text = {
                Text("This will safely disable ${app.packageName} without root (pm disable-user --user 0). You can re-enable it at any time.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDisableApp(app.packageName)
                        appToConfirmDisable = null
                    }
                ) {
                    Text("Disable App", color = TerminalAmber)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToConfirmDisable = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // TV Speed Booster Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Android TV Speed Tweaks") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Android TV devices often lag due to slow UI animation scales and background processes. Apply these instant tweaks:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Button(
                        onClick = {
                            onSpeedBoost(0.5f)
                            showSpeedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("⚡ 2x Speed Mode (0.5x Animations)")
                    }

                    Button(
                        onClick = {
                            onSpeedBoost(0.0f)
                            showSpeedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("🚀 Ultra Fast (Animations OFF)")
                    }

                    OutlinedButton(
                        onClick = {
                            onKillTasks()
                            showSpeedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🧹 Kill Cached Background Processes")
                    }

                    TextButton(
                        onClick = {
                            onSpeedBoost(1.0f)
                            showSpeedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Animations to Default (1.0x)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Sideload from GitHub / URL Dialog (like Obtainium)
    if (showUrlSideloadDialog) {
        AlertDialog(
            onDismissRequest = { showUrlSideloadDialog = false },
            icon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Download & Install from GitHub / URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Paste any direct APK link or GitHub Release asset URL to download and install directly on your Android TV:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = customApkUrl,
                        onValueChange = { customApkUrl = it },
                        placeholder = { Text("https://github.com/.../app.apk") },
                        label = { Text("APK URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        "Popular Android TV Apps (1-Tap):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            customApkUrl = "https://github.com/yuliskov/SmartTube/releases/latest/download/smarttube_stable.apk"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📺 SmartTube (Stable Release)")
                    }

                    OutlinedButton(
                        onClick = {
                            customApkUrl = "https://github.com/Gathin23/cleanyourtv/releases/latest/download/app-release.apk"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🧹 CleanYourTV Launcher")
                    }

                    OutlinedButton(
                        onClick = {
                            customApkUrl = "https://github.com/nova-video-player/aos-AVP/releases/latest/download/NovaVideoPlayer-universal-release.apk"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🎬 Nova Video Player")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customApkUrl.isNotBlank()) {
                            onDownloadAndInstallApk(customApkUrl.trim())
                            showUrlSideloadDialog = false
                        }
                    },
                    enabled = customApkUrl.isNotBlank()
                ) {
                    Text("Download & Sideload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlSideloadDialog = false }) {
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
    onUninstall: () -> Unit,
    onDisable: () -> Unit,
    onEnable: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isDisabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
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
                    color = when {
                        app.isDisabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        app.isSystem -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.label.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                app.isDisabled -> MaterialTheme.colorScheme.outline
                                app.isSystem -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
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
                            color = when {
                                app.isDisabled -> TerminalAmber.copy(alpha = 0.2f)
                                app.isSystem -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            }
                        ) {
                            Text(
                                text = when {
                                    app.isDisabled -> "Debloated"
                                    app.isSystem -> "System"
                                    else -> "User"
                                },
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                color = when {
                                    app.isDisabled -> TerminalAmber
                                    app.isSystem -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.secondary
                                }
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
                if (app.isDisabled) {
                    IconButton(
                        onClick = onEnable,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Re-enable App",
                            tint = TerminalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
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
                        onClick = onDisable,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Debloat / Disable",
                            tint = TerminalAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
