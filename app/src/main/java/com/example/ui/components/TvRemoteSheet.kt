package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvRemoteSheet(
    connectionState: ConnectionState,
    isTouchpadMode: Boolean = false,
    onToggleTouchpadMode: () -> Unit = {},
    onTouchTap: (Float, Float) -> Unit = { _, _ -> },
    onTouchSwipe: (Float, Float) -> Unit = { _, _ -> },
    onQuickScroll: (String) -> Unit = {},
    onKeyevent: (Int, String) -> Unit,
    onSendText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var textInput by remember { mutableStateOf("") }
    val isConnected = connectionState is ConnectionState.Connected
    var showPowerDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("tv_remote_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android TV Remote",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isConnected) {
                            (connectionState as ConnectionState.Connected).deviceModel ?: "Connected"
                        } else {
                            "Not Connected (Offline)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Mode Selector: D-Pad vs Touchpad (Mouse for Mobile Apps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = !isTouchpadMode,
                    onClick = { if (isTouchpadMode) onToggleTouchpadMode() },
                    label = { Text("D-Pad") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ControlCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = isTouchpadMode,
                    onClick = { if (!isTouchpadMode) onToggleTouchpadMode() },
                    label = { Text("Mouse / Touchpad") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick TV Actions Row (Power, Vol-, Mute, Vol+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteCircleButton(
                    icon = Icons.Default.PowerSettingsNew,
                    label = "Power",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { showPowerDialog = true }
                )
                RemoteCircleButton(
                    icon = Icons.Default.VolumeDown,
                    label = "Vol-",
                    onClick = { onKeyevent(25, "VOL_DOWN") }
                )
                RemoteCircleButton(
                    icon = Icons.Default.VolumeMute,
                    label = "Mute",
                    onClick = { onKeyevent(164, "MUTE") }
                )
                RemoteCircleButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Vol+",
                    onClick = { onKeyevent(24, "VOL_UP") }
                )
            }

            if (showPowerDialog) {
                AlertDialog(
                    onDismissRequest = { showPowerDialog = false },
                    icon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("TV Power & Reboot") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onKeyevent(26, "POWER")
                                    showPowerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Toggle Display Sleep / Wake")
                            }
                            Button(
                                onClick = {
                                    onKeyevent(26, "REBOOT")
                                    showPowerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reboot Android TV")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPowerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isTouchpadMode) {
                // Virtual Touchpad Surface for Mobile Apps
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val xPct = offset.x / size.width.toFloat()
                                        val yPct = offset.y / size.height.toFloat()
                                        onTouchTap(xPct, yPct)
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val dxPct = dragAmount.x / size.width.toFloat()
                                        val dyPct = dragAmount.y / size.height.toFloat()
                                        onTouchSwipe(dxPct, dyPct)
                                    }
                                )
                            }
                            .testTag("virtual_touchpad_surface"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Virtual TV Touchpad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to Click • Drag to Swipe / Move",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Mobile App Navigation Shortcuts (Scroll Up, Scroll Down, Left/Right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickScroll("up") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scroll ▲", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onQuickScroll("down") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Scroll ▼", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onKeyevent(23, "DPAD_CENTER") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Click (OK)", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Classic D-Pad Control
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        .testTag("tv_dpad_controller"),
                    contentAlignment = Alignment.Center
                ) {
                    // UP (19)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { onKeyevent(19, "DPAD_UP") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // DOWN (20)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { onKeyevent(20, "DPAD_DOWN") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // LEFT (21)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { onKeyevent(21, "DPAD_LEFT") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Left",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // RIGHT (22)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { onKeyevent(22, "DPAD_RIGHT") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // CENTER / SELECT (23)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onKeyevent(23, "DPAD_CENTER") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Row: Back, Home, Menu, Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteNavButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "Back",
                    onClick = { onKeyevent(4, "BACK") }
                )
                RemoteNavButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = { onKeyevent(3, "HOME") }
                )
                RemoteNavButton(
                    icon = Icons.Default.Menu,
                    label = "Menu",
                    onClick = { onKeyevent(82, "MENU") }
                )
                RemoteNavButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play/Pause",
                    onClick = { onKeyevent(85, "PLAY_PAUSE") }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Text injection to TV
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Send text to TV...", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tv_remote_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendText(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send text"
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteCircleButton(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RemoteNavButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
