package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdbCommandLog
import com.example.model.ConnectionState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TerminalConsoleBg
import com.example.ui.theme.TerminalConsoleHeader
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalScreen(
    connectionState: ConnectionState,
    commandLogs: List<AdbCommandLog>,
    commandHistory: List<String>,
    onExecuteCommand: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    var showHistoryMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isConnected = connectionState is ConnectionState.Connected

    val quickCommands = remember {
        listOf(
            "pm list packages -3" to "User Packages",
            "getprop ro.product.model" to "Device Model",
            "dumpsys window | grep -E 'mCurrentFocus'" to "Active App",
            "dumpsys power | grep mWakefulness" to "Power State",
            "df -h /data" to "Disk Usage",
            "ip route" to "IP Route",
            "input keyevent 26" to "Wake/Sleep",
            "input keyevent 3" to "TV Home",
            "input keyevent 4" to "TV Back",
            "input keyevent 23" to "TV Select"
        )
    }

    LaunchedEffect(commandLogs.size) {
        if (commandLogs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("terminal_screen")
    ) {
        // Quick Commands Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickCommands.forEach { (cmd, label) ->
                AssistChip(
                    onClick = {
                        commandInput = cmd
                        if (isConnected) {
                            onExecuteCommand(cmd)
                        }
                    },
                    label = { Text(label, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Terminal Console Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TerminalConsoleBg)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .testTag("terminal_console_box")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Console Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalConsoleHeader)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) TerminalGreen else TerminalRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) {
                                val conn = connectionState as ConnectionState.Connected
                                "shell@${conn.ip}:~$"
                            } else {
                                "shell@remote (offline)"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val allText = commandLogs.joinToString("\n\n") {
                                    "$ ${it.command}\n${it.output}"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Logs", allText))
                                Toast.makeText(context, "Terminal output copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy all logs",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear terminal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Log outputs
                if (commandLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pure Kotlin Wireless ADB Shell",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter an adb shell command below or tap a quick action",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    SelectionContainer {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(commandLogs, key = { it.id }) { log ->
                                TerminalLogItem(log = log)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Command Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = { showHistoryMenu = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Command History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showHistoryMenu,
                    onDismissRequest = { showHistoryMenu = false }
                ) {
                    if (commandHistory.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No command history yet") },
                            onClick = { showHistoryMenu = false }
                        )
                    } else {
                        commandHistory.take(10).forEach { histCmd ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = histCmd,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    commandInput = histCmd
                                    showHistoryMenu = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = {
                    Text(
                        text = if (isConnected) "Enter ADB shell command..." else "Connect TV first to run shell",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput)
                        commandInput = ""
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput)
                        commandInput = ""
                    }
                },
                enabled = commandInput.isNotBlank(),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("send_command_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute Command"
                )
            }
        }
    }
}

@Composable
private fun TerminalLogItem(log: AdbCommandLog) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Command header line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$ ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyanPrimary
                )
                Text(
                    text = log.command,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.executionTimeMs > 0) {
                    Text(
                        text = "${log.executionTimeMs}ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (log.isError) TerminalRed.copy(alpha = 0.25f) else TerminalGreen.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = if (log.exitCode == 0) "exit: 0" else "exit: ${log.exitCode}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (log.isError) TerminalRed else TerminalGreen,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Output body
        Text(
            text = log.output,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = if (log.isError) TerminalRed.copy(alpha = 0.9f) else Color(0xFFD4D4D4),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
