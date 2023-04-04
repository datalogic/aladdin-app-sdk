package com.datalogic.aladdinsdk.model

class BatteryManagementProfile {
    private lateinit var batterySource: String
    private var temperature = 0.0
    private var voltage = 0
    private var current = 0
    private var chargeLeft = 0

    fun getBatterySource(): String {
        return this.batterySource
    }

    fun getTemperature(): Double {
        return this.temperature
    }

    fun getVoltage(): Int {
        return this.voltage
    }

    fun getCurrent(): Int {
        return this.current
    }

    fun getChargeLeft(): Int {
        return this.chargeLeft
    }

    companion object {
        fun parseBatteryManagementResult(value: ByteArray): BatteryManagementProfile {
            //parse byte array to battery management profile
            val batteryManagementProfile = BatteryManagementProfile()
            batteryManagementProfile.batterySource = "None"
            batteryManagementProfile.temperature = 30.0
            batteryManagementProfile.voltage = 4268
            batteryManagementProfile.current = -221
            batteryManagementProfile.chargeLeft = 3850
            return batteryManagementProfile
        }
    }
}