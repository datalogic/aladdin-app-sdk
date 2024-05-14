package com.datalogic.aladdinsdk.interfaces

interface IServiceOutput {
    /**
     * Callback for the successful connection to the service
     */
    fun onServiceConnected()

    /**
     * Callback for a disconnection from the service.
     */
    fun onServiceDisconnected()
}