package com.datalogic.aladdinsdk.listener

import com.datalogic.aladdinsdk.model.Configuration

interface IConfigurationInterface {
    fun onConfigurationDataReceived(configuration: Configuration)
}