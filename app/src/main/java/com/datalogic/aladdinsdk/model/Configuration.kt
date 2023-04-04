package com.datalogic.aladdinsdk.model

import kotlin.properties.Delegates

class Configuration {
    private var vibration by Delegates.notNull<Boolean>()
    private var volume = 30
    private var proximity by Delegates.notNull<Boolean>()
    private lateinit var batteryManagementProfile: String
    private lateinit var standByDelay: String
    private var fuzzy1dProcessing by Delegates.notNull<Boolean>()
    private var displayMode by Delegates.notNull<Boolean>()
    private var pickingListMode by Delegates.notNull<Boolean>()
    private var unattendedMode by Delegates.notNull<Boolean>()

    fun getVibration(): Boolean {
        return this.vibration
    }

    fun getVolume(): Int {
        return this.volume
    }

    fun getProximity(): Boolean {
        return this.proximity
    }

    fun getBatteryManagementProfile(): String {
        return this.batteryManagementProfile
    }

    fun getStandByDelay(): String {
        return this.standByDelay
    }

    fun getFuzzy1dProcessing(): Boolean {
        return this.fuzzy1dProcessing
    }

    fun getDisplayMode(): Boolean {
        return this.displayMode
    }

    fun getPickingListMode(): Boolean {
        return this.pickingListMode
    }

    fun getUnattendedMode(): Boolean {
        return this.unattendedMode
    }

    companion object {
        fun parseConfiguration(value: ByteArray): Configuration {
            //parse byte array to configuration info
            val configurationInfo = Configuration()
            configurationInfo.vibration = true
            configurationInfo.volume = 30
            configurationInfo.proximity = false
            configurationInfo.batteryManagementProfile = "Profile 1"
            configurationInfo.standByDelay = "1 Minute"
            configurationInfo.fuzzy1dProcessing = false
            configurationInfo.pickingListMode = false
            configurationInfo.displayMode = false
            configurationInfo.unattendedMode = false
            return configurationInfo
        }
    }
}