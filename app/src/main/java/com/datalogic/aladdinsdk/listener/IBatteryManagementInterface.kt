package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.BatteryManagementProfile

/**
 * This Interface is used for sending battery information from hand scanner to aladdin
 */
interface IBatteryManagementInterface {
    fun onBatteryManagementDataReceived(batteryManagementProfile: BatteryManagementProfile)
}