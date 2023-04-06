package com.datalogic.aladdinsdk.model

/**
 * This class contains battery management profile fields
 */
class BatteryManagementProfile {
    private lateinit var batterySource: String
    private lateinit var temperature: String
    private lateinit var voltage: String
    private lateinit var current: String
    private lateinit var chargeLeft: String

    fun getBatterySource(): String {
        return this.batterySource
    }

    fun getTemperature(): String {
        return this.temperature
    }

    fun getVoltage(): String {
        return this.voltage
    }

    fun getCurrent(): String {
        return this.current
    }

    fun getChargeLeft(): String {
        return this.chargeLeft
    }

    companion object {
        /**
         * Parse battery information from bytearray to BatteryManagementProfile obj
         */
        fun parseBatteryManagementResult(value: ByteArray): BatteryManagementProfile {
            //TODO: parse byte array to battery management profile
            return getDummyBatteryManagementProfileData()
        }

        fun getDummyBatteryManagementProfileData(): BatteryManagementProfile {
            val batteryManagementProfile = BatteryManagementProfile()
            batteryManagementProfile.batterySource = "None"
            batteryManagementProfile.temperature = "30.0"
            batteryManagementProfile.voltage = "4268 MV"
            batteryManagementProfile.current = "-221 mA"
            batteryManagementProfile.chargeLeft = "3850 mAh"
            return batteryManagementProfile
        }
    }
}