// IMyAidlInterface.aidl
package com.datalogic.aladdinapp;

import com.datalogic.aladdinapp.ScannerEventListener;

interface IMyAidlInterface {

    void SubscribeScans(ScannerEventListener callback);

    void UnsubscribeScans(ScannerEventListener callback);

    String getQrCode();

    boolean isConnectedToScanner();

}