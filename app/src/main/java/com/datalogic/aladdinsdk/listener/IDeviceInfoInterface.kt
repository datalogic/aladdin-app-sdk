package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.DeviceInfo

interface IDeviceInfoInterface {
    fun onDeviceInfoDataReceived(deviceInfo: DeviceInfo)
}