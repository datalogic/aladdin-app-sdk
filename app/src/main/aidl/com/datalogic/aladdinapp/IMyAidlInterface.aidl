// IMyAidlInterface.aidl
package com.datalogic.aladdinapp;

import com.datalogic.aladdinapp.IRemoteServiceCallback;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void SubscribeScans(IRemoteServiceCallback callback);

    void UnsubscribeScans(IRemoteServiceCallback callback);

    String getQrCode();

    boolean isConnectedToScanner();

}