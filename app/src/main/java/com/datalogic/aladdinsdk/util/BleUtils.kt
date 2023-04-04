package com.datalogic.aladdinsdk.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

class BleUtils {
    companion object {
        fun getBleAdapter(context: Context): BluetoothAdapter {
            val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bleManager.adapter
        }
    }
}