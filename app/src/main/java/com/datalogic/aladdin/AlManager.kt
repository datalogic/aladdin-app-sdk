package com.datalogic.aladdin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import com.datalogic.aladdin.interfaces.IScannerOutput
import com.datalogic.aladdin.interfaces.IServiceOutput

open class AlManager() {
    private var connectedToService = false
    private var myService: IMyAidlInterface? = null
    private var iServiceOutput: IServiceOutput? = null
    private var iScannerOutput: IScannerOutput? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            LogUtils.debug("Client_app_Sdk", "onServiceConnected callback")
            myService = IMyAidlInterface.Stub.asInterface(service)
            connectedToService = true
            if (iServiceOutput != null) iServiceOutput!!.onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtils.debug("Client_app_Sdk", "onServiceDisConnected callback")
            if (iServiceOutput != null) iServiceOutput!!.onServiceDisconnected()
            connectedToService = false
        }
    }

    /*
    *Connect to the Aladdin App, if not connected already.
    * Triggers a Service Binding via (AIDL) IPC to the Aladdin App.
    * Will automatically re-establish the connection if necessary.
    * */
    fun ensureConnectionToService(context: Context): Boolean {
        if (!connectedToService) {
            val intent = Intent()
            intent.setClassName(
                "com.datalogic.aladdin", "com.datalogic.aladdin.data.model.EndlessService"
            )
            connectedToService = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            LogUtils.debug("Client_app_Sdk", "ensureConnectionToService")
        }
        return connectedToService
    }

    /*
    * To check scanner is connected to Aladdin app or not
    * */
    val isConnectedToScanner: Boolean
        get() {
            try {
                return if (myService != null) myService!!.isConnectedToScanner else false
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return false
        }

    /*
    * To check scanner is connected to Aladdin app or not
    * */
    val isConnectedToService: Boolean
        get() {
            return connectedToService
        }

    /*
   * Subscribe to Connection Information and Scanned Barcodes.
   * */
    fun subscribeToScans(scannerOutput: IScannerOutput?) {
        LogUtils.debug("Client_app_Sdk", "subscribeToScans")
        iScannerOutput = scannerOutput
        try {
            if (myService != null) myService!!.SubscribeScans(iScannerServiceCallback)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /*
   * Subscribe to Connection Information of the sdk-service.
   * */
    fun subscribeToServiceEvents(serviceOutput: IServiceOutput?) {
        iServiceOutput = serviceOutput
        LogUtils.debug("Client_app_Sdk", "subscribeToServiceEvents")
    }

    /*
   * Unsubscribe from Connection Information and Scanned Barcodes.
   * */
    fun unsubscribeFromScans() {
        LogUtils.debug("Client_app_Sdk", "unsubscribeFromScans")
        iScannerOutput = null
        try {
            LogUtils.debug("Client_app_Sdk", "SDK before unsubscribe enter")
            myService!!.UnsubscribeScans(iScannerServiceCallback)
            LogUtils.debug("Client_app_Sdk", "SDK before unsubscribe exit")
        } catch (e: RemoteException) {
            LogUtils.debug("Client_app_Sdk", e.message.toString())
            e.printStackTrace()
        }
    }

    /*
    * Unsubscribe Connection Information of the sdk-service.
    * */
    fun unsubscribeFromServiceEvents() {
        iServiceOutput = null
    }

    /*
    * To get last bar code value
    * */
    fun getLatestBarcodeData(): String {
        try {
            return myService!!.qrCode
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return "Error"
    }

    /*
    * Callback listeners to get Scan data
    * */
    var iScannerServiceCallback: IRemoteServiceCallback = object : IRemoteServiceCallback.Stub() {
        @Throws(RemoteException::class)
        override fun onBarcodeScanned(message: String) {
            LogUtils.debug("Client_app_Sdk", "onBarcodeScanned")
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onBarcodeScanned(message) }
        }

        @Throws(RemoteException::class)
        override fun onScannerConnected() {
            LogUtils.debug("Client_app_Sdk", "onScannerConnected")
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onScannerConnected() }
        }

        @Throws(RemoteException::class)
        override fun onScannerDisconnected() {
            LogUtils.debug("Client_app_Sdk", "onScannerDisconnected")
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onScannerDisconnected() }
        }
    }
}