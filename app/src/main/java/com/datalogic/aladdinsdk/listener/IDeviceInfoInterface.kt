package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.DeviceInfo

/**
 * This Interface is used for sending device information from hand scanner to aladdin
 */
interface IDeviceInfoInterface {
    fun onDeviceInfoDataReceived(deviceInfo: DeviceInfo)
}