package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.BatteryManagementProfile

interface IBatteryManagementInterface {
    fun onBatteryManagementDataReceived(batteryManagementProfile: BatteryManagementProfile)
}