// ScannerEventListener.aidl
package com.datalogic.aladdinapp;

// Declare any non-default types here with import statements

interface ScannerEventListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   void onBarcodeScanned(String barcode,String code,long scanTime);

   void onScannerConnected();

   void onScannerDisconnected();
}