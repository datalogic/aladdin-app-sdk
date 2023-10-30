// IRemoteServiceCallback.aidl
package com.datalogic.aladdinapp;

// Declare any non-default types here with import statements

interface IRemoteServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   void onBarcodeScanned(String message);

   void onScannerConnected();

   void onScannerDisconnected();

   void onScannerStateChanged();
}