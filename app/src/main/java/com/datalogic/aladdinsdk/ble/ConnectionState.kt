package com.datalogic.aladdinsdk.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.datalogic.aladdinsdk.constants.BLEConstants
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.ACTION_BLE_CONNECTION_STATE_CHANGED
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DEFAULT_CONNECTION_STATE
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.EXTRA_CONNECTION_STATE

@SuppressLint("StaticFieldLeak")
object ConnectionState {
    private var currentConnectionState = BLEConstants.DISCONNECTED
    private lateinit var mContext: Context
    private val bleBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED != action) {
                return
            }
            if (intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, DEFAULT_CONNECTION_STATE
                ) == BluetoothAdapter.STATE_OFF
            ) {
                setCurrentState(BLEConstants.DISCONNECTED)
            } else if (intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE, DEFAULT_CONNECTION_STATE
                ) == BluetoothAdapter.STATE_ON
            ) {
                //TODO: updating connection state as connected on bluetooth enable for demo, need to remove
                setCurrentState(BLEConstants.CONNECTED)
            }
        }
    }

    fun initConnectionState(context: Context?) {
        if (context != null) {
            mContext = context
            mContext.registerReceiver(
                bleBroadcastReceiver, IntentFilter(
                    BluetoothAdapter.ACTION_STATE_CHANGED
                )
            )
        }
    }

    fun setCurrentState(state: Int) {
        currentConnectionState = state
        val intent = Intent(ACTION_BLE_CONNECTION_STATE_CHANGED)
        intent.putExtra(EXTRA_CONNECTION_STATE, currentConnectionState)
        if (this::mContext.isInitialized) {
            mContext.sendBroadcast(intent)
        }
    }

    fun getCurrentState(): Int {
        return currentConnectionState
    }
}