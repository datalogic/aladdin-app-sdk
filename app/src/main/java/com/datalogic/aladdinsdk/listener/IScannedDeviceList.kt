package com.datalogic.aladdinsdk.listener

import com.polidea.rxandroidble3.RxBleDevice

/**
 * This Interface is used for getting ble device which are available to pair
 */
interface IScannedDeviceList {
    fun onDeviceAvailable(bleDevice: RxBleDevice)
}