package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.Configuration

/**
 * This Interface is used for sending configuration data from hand scanner to aladdin
 */
interface IConfigurationInterface {
    fun onConfigurationDataReceived(configuration: Configuration)
}