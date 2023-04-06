package com.datalogic.aladdinsdk.model

/**
 * This class contains Device info fields
 */
class DeviceInfo {
    private lateinit var deviceType: String
    private lateinit var firmwareVersion: String
    private lateinit var deviceId: String
    private lateinit var batteryManagementProfile: String

    fun getDeviceType(): String {
        return this.deviceType
    }

    fun getFirmwareVersion(): String {
        return this.firmwareVersion
    }

    fun getDeviceId(): String {
        return this.deviceId
    }

    fun getBatteryManagementProfile(): String {
        return this.batteryManagementProfile
    }

    companion object {
        /**
         * Parse device details from bytearray to DeviceInfo obj
         */
        fun parseDeviceInfo(value: ByteArray): DeviceInfo {
            //TODO:parse   array to device info
            return getDummyDeviceInfoData()
        }

        fun getDummyDeviceInfoData(): DeviceInfo {
            val deviceInfo = DeviceInfo()
            deviceInfo.deviceType = "Handscanner-1093e"
            deviceInfo.firmwareVersion = "v 1.0"
            deviceInfo.deviceId = "1093e"
            deviceInfo.batteryManagementProfile = "Battery Saver"
            return deviceInfo
        }
    }
}