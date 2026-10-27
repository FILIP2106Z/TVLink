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
    assertEquals("AdbCommander", appName)
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
}
