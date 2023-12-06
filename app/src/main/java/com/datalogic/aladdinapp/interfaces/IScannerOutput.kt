package com.datalogic.aladdinapp.interfaces

import com.datalogic.aladdinapp.model.BarcodeModel

interface IScannerOutput {
    /*
    * Callback Method that is called for each scanned barcode.
    * It will only be called once per Barcode.
    * */
    fun onBarcodeScanned(data: BarcodeModel?)
    /*
    * Notifier function that is called once an HHS is connected to Aladdin App.
    * */
    fun onScannerConnected()
    /*
    * Notifier function that is called once an HHS Scanner is disconnected from Aladdin App.
    * */
    fun onScannerDisconnected()
}