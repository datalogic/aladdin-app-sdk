package com.datalogic.aladdinapp.interfaces


interface IServiceOutput {
    /*
    * Callback for the successful ServiceConnection
    * */
    fun onServiceConnected()
    /*
    * Callback for a disconnection from the service.
    * */
    fun onServiceDisconnected()
}