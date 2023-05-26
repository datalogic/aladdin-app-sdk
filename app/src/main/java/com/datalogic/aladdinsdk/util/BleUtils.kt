package com.datalogic.aladdinsdk.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

/**
 * This class contains BLE util functions
 */
class BleUtils {
    companion object {
        /**
         * query for BLE adapter
         * @param context Context
         */
        fun getBleAdapter(context: Context): BluetoothAdapter {
            val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bleManager.adapter
        }

        /**
         * convert ASCII to byte array
         * @param asciiContent String
         * @return ByteArray after conversion
         */
        fun convertAsciiToByteArray(asciiContent: String): ByteArray {
            return asciiContent.toByteArray()
        }

        /**
         * convert byte array to ASCII content
         * @param byteArray ByteArray
         * @return ASCII content after conversion
         */
        fun convertByteArrayToAscii(byteArray: ByteArray): String {
            return String(byteArray)
        }
    }
}