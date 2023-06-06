package com.datalogic.aladdinsdk.listener

/**
 * This Interface is used for get the acknowledgement if the configuration data is received by hand scanner
 */
interface IConfigurationAck {
    fun onConfigurationDataReceived()
}