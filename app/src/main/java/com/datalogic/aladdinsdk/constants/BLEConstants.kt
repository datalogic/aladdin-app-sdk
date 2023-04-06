package com.datalogic.aladdinsdk.constants

import java.util.*

class BLEConstants {
    companion object {
        const val RECONNECTION_DELAY = 2000L
        val BATTERY_LEVEL_UUID: UUID? = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val BATTERY_MANAGEMENT_UUID: UUID? = UUID.randomUUID()
        val DEVICE_INFO_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_SCANNED_UUID: UUID? = UUID.randomUUID()
        const val isShowDummy = true
    }
}