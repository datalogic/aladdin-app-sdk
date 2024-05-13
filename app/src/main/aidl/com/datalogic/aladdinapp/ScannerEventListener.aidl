// ScannerEventListener.aidl
package com.datalogic.aladdinapp;

interface ScannerEventListener {

    void onBarcodeScanned(String barcode,String code,long scanTime);

    void onScannerConnected();

    void onScannerDisconnected();
}