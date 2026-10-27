package com.example.model

import java.util.UUID

data class DeviceConnection(
    val ip: String,
    val port: Int = 5555,
    val label: String = "",
    val lastConnectedTimestamp: Long = System.currentTimeMillis()
)

data class DiscoveredDevice(
    val name: String,
    val ip: String,
    val port: Int = 5555,
    val source: String, // "mDNS (NSD)" or "Subnet Scan"
    val discoveredAt: Long = System.currentTimeMillis()
)

data class AdbCommandLog(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val output: String,
    val exitCode: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = exitCode != 0,
    val executionTimeMs: Long = 0L
)

data class InstalledApp(
    val packageName: String,
    val isSystem: Boolean,
    val isDisabled: Boolean = false,
    val label: String = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() },
    val versionName: String = ""
)

sealed interface ConnectionState {
    data class Disconnected(val message: String? = null) : ConnectionState
    data class Connecting(val ip: String, val port: Int) : ConnectionState
    data class Connected(
        val ip: String,
        val port: Int,
        val deviceModel: String?,
        val androidVersion: String?,
        val latencyMs: Long
    ) : ConnectionState
    data class Error(val message: String, val ip: String? = null, val port: Int? = null) : ConnectionState
}
