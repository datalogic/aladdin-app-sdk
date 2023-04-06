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
    }
}