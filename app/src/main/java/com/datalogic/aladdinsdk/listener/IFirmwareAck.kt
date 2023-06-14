package com.datalogic.aladdinsdk.listener

/**
 * This Interface is used for get the acknowledgement if the firmware upgrade is completed by hand scanner
 */
interface IFirmwareAck {
    fun onFirmwareUpgradeCompleted()

    fun onFirmwareErrorOccurred()
}