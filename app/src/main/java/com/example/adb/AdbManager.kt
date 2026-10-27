package com.example.adb

import android.content.Context
import android.util.Log
import com.example.model.AdbCommandLog
import com.example.model.ConnectionState
import com.example.model.InstalledApp
import dadb.AdbKeyPair
import dadb.AdbShellResponse
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

class AdbManager(private val context: Context) {

    private val TAG = "AdbManager"

    private var currentDadb: Dadb? = null
    private var activeIp: String = ""
    private var activePort: Int = 5555

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val keyPair: AdbKeyPair by lazy {
        getOrCreateKeyPair()
    }

    private fun getOrCreateKeyPair(): AdbKeyPair {
        val privKeyFile = File(context.filesDir, "adbkey")
        val pubKeyFile = File(context.filesDir, "adbkey.pub")
        if (!privKeyFile.exists() || !pubKeyFile.exists()) {
            try {
                AdbKeyPair.generate(privKeyFile, pubKeyFile)
                Log.d(TAG, "Generated new ADB KeyPair in ${context.filesDir.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating ADB KeyPair", e)
            }
        }
        return AdbKeyPair.read(privKeyFile, pubKeyFile)
    }

    suspend fun connect(ip: String, port: Int = 5555): Result<ConnectionState.Connected> =
        withContext(Dispatchers.IO) {
            val cleanIp = ip.trim()
            if (cleanIp.isBlank()) {
                val err = "Target IP address cannot be empty"
                _connectionState.value = ConnectionState.Error(err, cleanIp, port)
                return@withContext Result.failure(IllegalArgumentException(err))
            }

            _connectionState.value = ConnectionState.Connecting(cleanIp, port)
            try {
                // Close existing connection if any
                disconnect()

                val startTime = System.currentTimeMillis()
                Log.d(TAG, "Connecting to $cleanIp:$port...")
                val dadb = Dadb.create(cleanIp, port, keyPair)
                val latency = System.currentTimeMillis() - startTime

                // Test connection with a lightweight shell command
                val modelResp = dadb.shell("getprop ro.product.model")
                val versionResp = dadb.shell("getprop ro.build.version.release")

                val model = modelResp.allOutput.trim().ifBlank { "Android TV Device" }
                val androidVersion = versionResp.allOutput.trim().ifBlank { null }

                currentDadb = dadb
                activeIp = cleanIp
                activePort = port

                val connectedState = ConnectionState.Connected(
                    ip = cleanIp,
                    port = port,
                    deviceModel = model,
                    androidVersion = androidVersion,
                    latencyMs = latency
                )
                _connectionState.value = connectedState
                Log.d(TAG, "Connected successfully to $cleanIp:$port ($model)")
                Result.success(connectedState)
            } catch (e: ConnectException) {
                Log.e(TAG, "Connection refused: $cleanIp:$port", e)
                val msg = "Connection refused at $cleanIp:$port. Ensure Wireless Debugging is enabled on your TV."
                _connectionState.value = ConnectionState.Error(msg, cleanIp, port)
                Result.failure(Exception(msg, e))
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Connection timed out: $cleanIp:$port", e)
                val msg = "Connection timed out at $cleanIp:$port. Make sure the TV is turned on and on the same Wi-Fi."
                _connectionState.value = ConnectionState.Error(msg, cleanIp, port)
                Result.failure(Exception(msg, e))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to $cleanIp:$port", e)
                val errorMsg = when {
                    e.message?.contains("unauthorized", ignoreCase = true) == true ->
                        "Target device unauthorized. Check your TV screen and select 'Always allow from this computer'."
                    e.message?.contains("broken pipe", ignoreCase = true) == true ->
                        "Connection broken. The TV closed the connection."
                    else -> e.message ?: "Failed to connect to $cleanIp:$port"
                }
                _connectionState.value = ConnectionState.Error(errorMsg, cleanIp, port)
                Result.failure(Exception(errorMsg, e))
            }
        }

    fun disconnect() {
        try {
            currentDadb?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing dadb", e)
        } finally {
            currentDadb = null
            activeIp = ""
            _connectionState.value = ConnectionState.Disconnected("Disconnected")
        }
    }

    fun isConnected(): Boolean {
        return currentDadb != null && _connectionState.value is ConnectionState.Connected
    }

    suspend fun executeShell(command: String): AdbCommandLog = withContext(Dispatchers.IO) {
        val dadb = currentDadb
            ?: return@withContext AdbCommandLog(
                command = command,
                output = "Error: Not connected to any Android TV device. Connect first.",
                exitCode = -1,
                isError = true
            )

        val startTime = System.currentTimeMillis()
        try {
            val response: AdbShellResponse = dadb.shell(command)
            val duration = System.currentTimeMillis() - startTime
            val output = response.allOutput.ifEmpty { "(Command executed with no output)" }
            AdbCommandLog(
                command = command,
                output = output,
                exitCode = response.exitCode,
                isError = response.exitCode != 0,
                executionTimeMs = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "Shell execution error for: $command", e)
            AdbCommandLog(
                command = command,
                output = "Execution failed: ${e.message ?: "Unknown error"}",
                exitCode = -1,
                isError = true,
                executionTimeMs = duration
            )
        }
    }

    suspend fun sendKeyevent(keyCode: Int): Boolean = withContext(Dispatchers.IO) {
        val dadb = currentDadb ?: return@withContext false
        try {
            val res = dadb.shell("input keyevent $keyCode")
            res.exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error sending keyevent $keyCode", e)
            false
        }
    }

    suspend fun sendText(text: String): Boolean = withContext(Dispatchers.IO) {
        val dadb = currentDadb ?: return@withContext false
        try {
            // Escape special shell characters
            val escaped = text.replace(" ", "%s").replace("\"", "\\\"")
            val res = dadb.shell("input text \"$escaped\"")
            res.exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error sending text", e)
            false
        }
    }

    suspend fun pingLatency(): Long? = withContext(Dispatchers.IO) {
        val dadb = currentDadb ?: return@withContext null
        try {
            val start = System.currentTimeMillis()
            val res = dadb.shell("echo ping")
            if (res.exitCode == 0) {
                val latency = System.currentTimeMillis() - start
                // Update connected state with new latency
                val current = _connectionState.value
                if (current is ConnectionState.Connected) {
                    _connectionState.value = current.copy(latencyMs = latency)
                }
                latency
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getInstalledApps(includeSystem: Boolean = false): List<InstalledApp> =
        withContext(Dispatchers.IO) {
            val dadb = currentDadb ?: return@withContext emptyList()
            val apps = mutableListOf<InstalledApp>()

            try {
                // Fetch disabled packages to mark debloated apps
                val disabledResponse = dadb.shell("pm list packages -d")
                val disabledPkgs = parsePackages(disabledResponse.allOutput)

                // Fetch 3rd party user installed apps
                val userPkgsResponse = dadb.shell("pm list packages -3")
                val userPkgs = parsePackages(userPkgsResponse.allOutput)
                userPkgs.forEach { pkg ->
                    apps.add(
                        InstalledApp(
                            packageName = pkg,
                            isSystem = false,
                            isDisabled = disabledPkgs.contains(pkg)
                        )
                    )
                }

                if (includeSystem) {
                    // Fetch system apps
                    val sysPkgsResponse = dadb.shell("pm list packages -s")
                    val sysPkgs = parsePackages(sysPkgsResponse.allOutput)
                    sysPkgs.forEach { pkg ->
                        if (!userPkgs.contains(pkg)) {
                            apps.add(
                                InstalledApp(
                                    packageName = pkg,
                                    isSystem = true,
                                    isDisabled = disabledPkgs.contains(pkg)
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting installed packages", e)
            }

            apps.sortedBy { it.label.lowercase() }
        }

    private fun parsePackages(output: String): Set<String> {
        val result = mutableSetOf<String>()
        output.lines().forEach { line ->
            val clean = line.trim()
            if (clean.startsWith("package:")) {
                val pkgName = clean.removePrefix("package:").trim()
                if (pkgName.isNotBlank()) {
                    result.add(pkgName)
                }
            }
        }
        return result
    }

    suspend fun forceStopApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("am force-stop $packageName")
        !res.isError
    }

    suspend fun clearAppData(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("pm clear $packageName")
        !res.isError && res.output.contains("Success", ignoreCase = true)
    }

    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("monkey -p $packageName -c android.intent.category.LAUNCHER 1")
        !res.isError
    }

    suspend fun uninstallApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("pm uninstall $packageName")
        !res.isError && res.output.contains("Success", ignoreCase = true)
    }

    suspend fun disableApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("pm disable-user --user 0 $packageName")
        !res.isError && (res.output.contains("disabled", ignoreCase = true) || res.output.contains("new state", ignoreCase = true))
    }

    suspend fun enableApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("pm enable $packageName")
        !res.isError && (res.output.contains("enabled", ignoreCase = true) || res.output.contains("new state", ignoreCase = true))
    }

    suspend fun installApk(file: File): Result<String> = withContext(Dispatchers.IO) {
        val dadb = currentDadb ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            dadb.install(file)
            Result.success("APK installed successfully on Android TV")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK", e)
            Result.failure(e)
        }
    }

    suspend fun setAnimationScale(scale: Float): Boolean = withContext(Dispatchers.IO) {
        val dadb = currentDadb ?: return@withContext false
        try {
            dadb.shell("settings put global window_animation_scale $scale")
            dadb.shell("settings put global transition_animation_scale $scale")
            dadb.shell("settings put global animator_duration_scale $scale")
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun killBackgroundProcesses(): Boolean = withContext(Dispatchers.IO) {
        val res = executeShell("am kill-all")
        !res.isError
    }

    suspend fun rebootDevice(mode: String = ""): Boolean = withContext(Dispatchers.IO) {
        val cmd = if (mode.isNotBlank()) "reboot $mode" else "reboot"
        val res = executeShell(cmd)
        !res.isError
    }
}
