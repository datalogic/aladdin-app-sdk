package com.datalogic.aladdinsdk.model

/**
 * This class contains Configuration fields
 */
class Configuration {
    private var vibration = false
    private var volume = 30
    private var proximity = false
    private lateinit var batteryManagementProfile: String
    private lateinit var standByDelay: String
    private var fuzzy1dProcessing = false
    private var displayMode = false
    private var pickingListMode = false
    private var unattendedMode = false

    fun getVibration(): Boolean {
        return this.vibration
    }

    fun setVibration(value: Boolean) {
        this.vibration = value
    }

    fun getVolume(): Int {
        return this.volume
    }

    fun setVolume(value: Int) {
        this.volume = value
    }

    fun getProximity(): Boolean {
        return this.proximity
    }

    fun setProximity(value: Boolean) {
        this.proximity = value
    }

    fun getBatteryManagementProfile(): String {
        return this.batteryManagementProfile
    }

    fun setBatteryManagementProfile(value: String) {
        this.batteryManagementProfile = value
    }

    fun getStandByDelay(): String {
        return this.standByDelay
    }

    fun setStandByDelay(value: String) {
        this.standByDelay = value
    }

    fun getFuzzy1dProcessing(): Boolean {
        return this.fuzzy1dProcessing
    }

    fun setFuzzy1dProcessing(value: Boolean) {
        this.fuzzy1dProcessing = value
    }

    fun getDisplayMode(): Boolean {
        return this.displayMode
    }

    fun setDisplayMode(value: Boolean) {
        this.displayMode = value
    }

    fun getPickingListMode(): Boolean {
        return this.pickingListMode
    }

    fun setPickingListMode(value: Boolean) {
        this.pickingListMode = value
    }

    fun getUnattendedMode(): Boolean {
        return this.unattendedMode
    }

    fun setUnattendedMode(value: Boolean) {
        this.unattendedMode = value
    }

    companion object {
        /**
         * Parse configuration details from bytearray to Configuration obj
         */
        fun parseConfiguration(value: ByteArray): Configuration {
            //TODO: parse byte array to configuration info
            return getDummyConfigurationData()
        }

        fun getDummyConfigurationData(): Configuration {
            val configuration = Configuration()
            configuration.vibration = true
            configuration.volume = 30
            configuration.proximity = false
            configuration.batteryManagementProfile = "Profile 1"
            configuration.standByDelay = "1 Minute"
            configuration.fuzzy1dProcessing = false
            configuration.pickingListMode = false
            configuration.displayMode = false
            configuration.unattendedMode = false
            return configuration
        }
    }
}