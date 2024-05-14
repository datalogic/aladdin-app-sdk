package com.datalogic.aladdinsdk.interfaces

import com.datalogic.aladdinsdk.model.BarcodeModel

interface IScannerOutput {
    /**
     * Callback method that is called for each scanned barcode.
     * It will only be called once per Barcode.
     */
    fun onBarcodeScanned(data: BarcodeModel?)

    /**
     * Callback method that is called once an HHS is connected to Aladdin App.
     */
    fun onScannerConnected()

    /**
     * Callback method that is called once an HHS Scanner is disconnected from Aladdin App.
     */
    fun onScannerDisconnected()
}