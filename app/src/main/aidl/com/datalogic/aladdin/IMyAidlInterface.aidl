// IMyAidlInterface.aidl
package com.datalogic.aladdin;

import com.datalogic.aladdin.ScannerEventListener;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void SubscribeScans(ScannerEventListener callback);

    void UnsubscribeScans(ScannerEventListener callback);

    String getQrCode();

    boolean isConnectedToScanner();

}