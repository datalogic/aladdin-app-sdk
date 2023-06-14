package com.datalogic.aladdinsdk.constants

import java.util.*

class BLEConstants {
    companion object {
        const val RECONNECTION_DELAY = 2000L
        const val isShowDummy = true
        const val EMPTY_STRING = ""
        const val NUMERIC_1 = 1
        const val CONNECTED = 1
        const val CONNECTING = 2
        const val DISCONNECTED = 3
        const val DEFAULT_CONNECTION_STATE = -1
        const val ACTION_BLE_CONNECTION_STATE_CHANGED = "action_ble_connection_state_changed"
        const val EXTRA_CONNECTION_STATE = "extra_connection_state"
        const val REQUEST_CONFIGURATION_COMMAND = 0x02
        const val CONFIGURATION_MESSAGE_TX_COMPLETE = 0x04
        const val ANSWER_CONFIGURATION_COMMAND = 0x01
        const val CONFIGURATION_TAG_C = "C"
        const val DATA_LEN_INFO_SHORT_LENGTH = 0xC4
        const val DATA_LEN_INFO_LONG_LENGTH = 0xC5
        val OUTPUT_SEGMENT_CHARACTERISTIC_UUID = UUID.fromString("c17b0002-db81-42a2-9e5a-64e1df0f05f9")
        val INPUT_SEGMENT_CHARACTERISTIC_UUID = UUID.fromString("c17b0003-db81-42a2-9e5a-64e1df0f05f9")
        val BATTERY_LEVEL_UUID: UUID? = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        val BATTERY_MANAGEMENT_UUID: UUID? = UUID.randomUUID()
        val DEVICE_INFO_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_UUID: UUID? = UUID.randomUUID()
        val CONFIGURATION_SCANNED_UUID: UUID? = UUID.randomUUID()

        enum class ConfigurationFeature(val featureId: String) {
            VOLUME("BPVO")
        }
    }
}