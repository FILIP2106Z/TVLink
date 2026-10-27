package com.example.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

class DeviceDiscoveryService(private val context: Context) {

    private val TAG = "DeviceDiscovery"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var scanJob: Job? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isNsdRegistered = false

    fun startDiscovery() {
        if (_isScanning.value) return
        _isScanning.value = true

        scanJob = scope.launch {
            // 1. Start mDNS Network Service Discovery
            startNsdDiscovery()

            // 2. Start Subnet Sweep on Port 5555
            runSubnetSweep()

            _isScanning.value = false
        }
    }

    fun stopDiscovery() {
        scanJob?.cancel()
        scanJob = null
        stopNsdDiscovery()
        _isScanning.value = false
    }

    private fun addDevice(device: DiscoveredDevice) {
        _discoveredDevices.update { current ->
            // Deduplicate by IP and Port
            val existing = current.indexOfFirst { it.ip == device.ip && it.port == device.port }
            if (existing >= 0) {
                current.toMutableList().apply {
                    this[existing] = device
                }
            } else {
                current + device
            }
        }
    }

    private fun startNsdDiscovery() {
        if (nsdManager == null || isNsdRegistered) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String?) {
                Log.d(TAG, "NSD Service discovery started: $regType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "NSD Service found: ${serviceInfo.serviceName}")
                try {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.w(TAG, "Resolve failed for ${serviceInfo?.serviceName}: error $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                            val host = resolvedInfo.host?.hostAddress ?: return
                            val port = resolvedInfo.port
                            val name = resolvedInfo.serviceName ?: "Android TV"
                            addDevice(
                                DiscoveredDevice(
                                    name = name,
                                    ip = host,
                                    port = if (port > 0) port else 5555,
                                    source = "mDNS (NSD)"
                                )
                            )
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Error resolving service", e)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped: $serviceType")
                isNsdRegistered = false
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
                isNsdRegistered = false
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop Discovery failed: Error code: $errorCode")
                isNsdRegistered = false
            }
        }

        try {
            nsdManager.discoverServices("_adb._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            isNsdRegistered = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start NSD discovery", e)
        }
    }

    private fun stopNsdDiscovery() {
        if (nsdManager != null && isNsdRegistered && discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop NSD discovery", e)
            }
            isNsdRegistered = false
        }
    }

    private suspend fun runSubnetSweep() = withContext(Dispatchers.IO) {
        val subnetBase = getLocalSubnetPrefix() ?: return@withContext
        Log.d(TAG, "Starting subnet sweep on $subnetBase.*:5555")

        val semaphore = Semaphore(25) // Up to 25 parallel socket probes
        val jobs = mutableListOf<Job>()

        for (hostNum in 1..254) {
            val targetIp = "$subnetBase.$hostNum"
            val job = launch {
                semaphore.withPermit {
                    if (probeAdbPort(targetIp, 5555, 250)) {
                        val hostName = try {
                            val addr = InetAddress.getByName(targetIp)
                            val canonical = addr.canonicalHostName
                            if (canonical != targetIp) canonical else "Android TV ($targetIp)"
                        } catch (_: Exception) {
                            "Android TV ($targetIp)"
                        }
                        addDevice(
                            DiscoveredDevice(
                                name = hostName,
                                ip = targetIp,
                                port = 5555,
                                source = "Subnet Scan"
                            )
                        )
                    }
                }
            }
            jobs.add(job)
        }

        jobs.forEach { it.join() }
    }

    private fun probeAdbPort(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getLocalSubnetPrefix(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            val lastDot = ip.lastIndexOf('.')
                            if (lastDot > 0) {
                                return ip.substring(0, lastDot)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get local IP subnet", e)
        }
        return null
    }
}
