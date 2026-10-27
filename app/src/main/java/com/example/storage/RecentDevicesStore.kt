package com.example.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DeviceConnection
import org.json.JSONArray
import org.json.JSONObject

class RecentDevicesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("adbtv_remote_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT_DEVICES = "recent_devices_json"
        private const val KEY_COMMAND_HISTORY = "command_history_json"
        private const val KEY_LAST_IP = "last_connected_ip"
        private const val KEY_LAST_PORT = "last_connected_port"
        private const val MAX_RECENT = 20
        private const val MAX_HISTORY = 40
    }

    fun getRecentDevices(): List<DeviceConnection> {
        val jsonStr = prefs.getString(KEY_RECENT_DEVICES, null) ?: return emptyList()
        val list = mutableListOf<DeviceConnection>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DeviceConnection(
                        ip = obj.getString("ip"),
                        port = obj.optInt("port", 5555),
                        label = obj.optString("label", ""),
                        lastConnectedTimestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return list.sortedByDescending { it.lastConnectedTimestamp }
    }

    fun saveDevice(ip: String, port: Int = 5555, label: String = "") {
        val current = getRecentDevices().toMutableList()
        // Remove existing device with same IP and port if exists
        val existingIndex = current.indexOfFirst { it.ip == ip && it.port == port }
        val finalLabel = if (label.isNotBlank()) {
            label
        } else if (existingIndex >= 0 && current[existingIndex].label.isNotBlank()) {
            current[existingIndex].label
        } else {
            "Android TV"
        }

        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        }

        current.add(
            0,
            DeviceConnection(
                ip = ip,
                port = port,
                label = finalLabel,
                lastConnectedTimestamp = System.currentTimeMillis()
            )
        )

        val trimmed = current.take(MAX_RECENT)
        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject()
            obj.put("ip", item.ip)
            obj.put("port", item.port)
            obj.put("label", item.label)
            obj.put("timestamp", item.lastConnectedTimestamp)
            array.put(obj)
        }

        prefs.edit()
            .putString(KEY_RECENT_DEVICES, array.toString())
            .putString(KEY_LAST_IP, ip)
            .putInt(KEY_LAST_PORT, port)
            .apply()
    }

    fun updateDeviceLabel(ip: String, port: Int, newLabel: String) {
        val current = getRecentDevices().map {
            if (it.ip == ip && it.port == port) it.copy(label = newLabel) else it
        }
        val array = JSONArray()
        for (item in current) {
            val obj = JSONObject()
            obj.put("ip", item.ip)
            obj.put("port", item.port)
            obj.put("label", item.label)
            obj.put("timestamp", item.lastConnectedTimestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_DEVICES, array.toString()).apply()
    }

    fun removeDevice(ip: String, port: Int) {
        val filtered = getRecentDevices().filterNot { it.ip == ip && it.port == port }
        val array = JSONArray()
        for (item in filtered) {
            val obj = JSONObject()
            obj.put("ip", item.ip)
            obj.put("port", item.port)
            obj.put("label", item.label)
            obj.put("timestamp", item.lastConnectedTimestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_DEVICES, array.toString()).apply()
    }

    fun getLastConnected(): Pair<String, Int> {
        val ip = prefs.getString(KEY_LAST_IP, "") ?: ""
        val port = prefs.getInt(KEY_LAST_PORT, 5555)
        return ip to port
    }

    fun getCommandHistory(): List<String> {
        val jsonStr = prefs.getString(KEY_COMMAND_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun addCommandToHistory(command: String) {
        val trimmedCmd = command.trim()
        if (trimmedCmd.isBlank()) return
        val current = getCommandHistory().toMutableList()
        current.remove(trimmedCmd)
        current.add(0, trimmedCmd)
        val trimmed = current.take(MAX_HISTORY)
        val array = JSONArray()
        trimmed.forEach { array.put(it) }
        prefs.edit().putString(KEY_COMMAND_HISTORY, array.toString()).apply()
    }
}
