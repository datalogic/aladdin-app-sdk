package com.datalogic.aladdinapp.interfaces;

public interface IScannerOutput {
    void onBarcodeScanned(String data);
    void onScannerConnected();
    void onScannerDisconnected();
}
