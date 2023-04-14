package com.datalogic.aladdinsdk.constants

import java.util.*

class BLEConstants {
    companion object {
        const val RECONNECTION_DELAY = 2000L
        const val isShowDummy = true
        const val CONNECTED = 1
        const val CONNECTING = 2
        const val DISCONNECTED = 3
        const val DEFAULT_CONNECTION_STATE = -1
        const val ACTION_BLE_CONNECTION_STATE_CHANGED = "action_ble_connection_state_changed"
        const val EXTRA_CONNECTION_STATE = "extra_connection_state"
        val BATTERY_LEVEL_UUID: UUID? = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val BATTERY_MANAGEMENT_UUID: UUID? = UUID.randomUUID()
        val DEVICE_INFO_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_SCANNED_UUID: UUID? = UUID.randomUUID()
    }
}