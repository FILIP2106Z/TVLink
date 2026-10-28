package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.storage.RecentDevicesStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TVLink", appName)
  }

  @Test
  fun `save and retrieve recent devices`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = RecentDevicesStore(context)
    store.saveDevice("192.168.1.150", 5555, "Living Room TV")

    val devices = store.getRecentDevices()
    assertTrue(devices.any { it.ip == "192.168.1.150" && it.label == "Living Room TV" })

    store.updateDeviceLabel("192.168.1.150", 5555, "Shield TV")
    val updated = store.getRecentDevices()
    assertTrue(updated.any { it.ip == "192.168.1.150" && it.label == "Shield TV" })
  }

  @Test
  fun `verify adb manager install diagnostics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.adb.AdbManager(context)

    // Test ABI Mismatch
    val abiDiag = manager.parseInstallFailure("Failure [INSTALL_FAILED_NO_MATCHING_ABIS: Failed to extract native libraries]")
    assertTrue(abiDiag != null)
    assertTrue(abiDiag!!.userFriendlyMessage.contains("Architecture mismatch"))
    assertEquals(false, abiDiag.isFallbackCandidate)

    // Test Insufficient Storage
    val storageDiag = manager.parseInstallFailure("Failure [INSTALL_FAILED_INSUFFICIENT_STORAGE]")
    assertTrue(storageDiag != null)
    assertTrue(storageDiag!!.userFriendlyMessage.contains("Insufficient storage"))

    // Test Downgrade Error
    val downgradeDiag = manager.parseInstallFailure("Failure [INSTALL_FAILED_VERSION_DOWNGRADE]")
    assertTrue(downgradeDiag != null)
    assertTrue(downgradeDiag!!.userFriendlyMessage.contains("Downgrade blocked"))

    // Test Timeout Fallback Candidate
    val timeoutDiag = manager.parseInstallFailure("java.net.SocketTimeoutException: Read timed out")
    assertTrue(timeoutDiag != null)
    assertEquals(true, timeoutDiag!!.isFallbackCandidate)
  }
}
