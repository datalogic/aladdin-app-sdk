package com.datalogic.aladdinsdk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import com.datalogic.aladdinapp.IMyAidlInterface
import com.datalogic.aladdinapp.ScannerEventListener
import com.datalogic.aladdinsdk.interfaces.IScannerOutput
import com.datalogic.aladdinsdk.interfaces.IServiceOutput
import com.datalogic.aladdinsdk.model.BarcodeModel

@Suppress("unused")
open class AlManager {
    private var connectedToService = false
    private var myService: IMyAidlInterface? = null
    private var iServiceOutput: IServiceOutput? = null
    private var iScannerOutput: IScannerOutput? = null
    private var iContext: Context? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            LogUtils.debug("onServiceConnected callback")
            myService = IMyAidlInterface.Stub.asInterface(service)
            connectedToService = true
            iServiceOutput?.onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtils.debug("onServiceDisConnected callback")
            iServiceOutput?.onServiceDisconnected()
            connectedToService = false
        }
    }

    /**
     * Connect to the Aladdin app, if not connected already.
     * Triggers a Service Binding via (AIDL) IPC to the Aladdin App.
     * Will automatically re-establish the connection if necessary.
     */
    @Suppress("unused")
    fun connectToService(context: Context): Boolean {
        if (!connectedToService) {
            iContext = context
            val intent = Intent()
            intent.setClassName(
                "com.datalogic.aladdinapp", "com.datalogic.aladdinapp.data.model.EndlessService"
            )
            LogUtils.debug("connectToService")
            return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        return true
    }


    /**
     * Disconnect from the Aladdin app.
     */
    @Suppress("unused")
    fun disconnectFromService() {
        LogUtils.debug("disconnectFromService")
        iContext?.unbindService(connection)
        connectedToService = false
        unsubscribeFromScans()
        unsubscribeFromServiceEvents()
        LogUtils.debug("unBindFromService")
    }

    /**
     * Checks if the scanner is connected to Aladdin app.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val isConnectedToScanner: Boolean
        get() {
            LogUtils.debug("isConnectedToScanner")
            try {
                myService?.let { return it.isConnectedToScanner }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return false
        }

    /**
     * Checks if the service is currently connected.
     */
    @Suppress("unused")
    val isConnectedToService: Boolean
        get() {
            return connectedToService
        }

    /**
     * Subscribe to Connection Information and Scanned Barcodes.
     */
    @Suppress("unused")
    fun subscribeToScans(scannerOutput: IScannerOutput?) {
        LogUtils.debug("subscribeToScans")
        iScannerOutput = scannerOutput
        try {
            val service = myService
            if (service != null) {
                service.SubscribeScans(iScannerServiceCallback)
            } else {
                LogUtils.debug("subscribeToScans myService null")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    /**
     * Subscribe to Connection Information of the sdk-service.
     */
    @Suppress("unused")
    fun subscribeToServiceEvents(serviceOutput: IServiceOutput?) {
        iServiceOutput = serviceOutput
        LogUtils.debug("subscribeToServiceEvents")
    }

    /**
     * Unsubscribe from Connection Information and Scanned Barcodes.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun unsubscribeFromScans() {
        LogUtils.debug("unsubscribeFromScans")
        iScannerOutput = null
        try {
            LogUtils.debug("SDK before unsubscribe enter")
            val service = myService
            if (service != null) {
                service.UnsubscribeScans(iScannerServiceCallback)
            } else {
                LogUtils.debug("unsubscribeFromScans myService null")
            }

            LogUtils.debug("SDK before unsubscribe exit")
        } catch (e: RemoteException) {
            LogUtils.debug(e.message.toString())
            e.printStackTrace()
        }
    }

    /**
     * Unsubscribe Connection Information of the sdk-service.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun unsubscribeFromServiceEvents() {
        iServiceOutput = null
    }

    /**
     * Retrieve the last barcode value.
     */
    @Suppress("unused")
    fun getLatestBarcodeData(): String {
        try {
            myService?.let { return it.qrCode }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return ""
    }

    /*
    * Callback listeners to get Scan data
    * */
    private var iScannerServiceCallback: ScannerEventListener = object : ScannerEventListener.Stub() {
        @Throws(RemoteException::class)

        override fun onBarcodeScanned(barcode: String?, code: String?, scanTime: Long) {
            LogUtils.debug("onBarcodeScanned")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                val barCodeModel = BarcodeModel()
                barCodeModel.barcode = barcode
                barCodeModel.code = code
                barCodeModel.scanTime = scanTime
                iScannerOutput?.onBarcodeScanned(barCodeModel)
            }
        }

        @Throws(RemoteException::class)
        override fun onScannerConnected() {
            LogUtils.debug("onScannerConnected")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post { iScannerOutput?.onScannerConnected() }
        }

        @Throws(RemoteException::class)
        override fun onScannerDisconnected() {
            LogUtils.debug("onScannerDisconnected")
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post { iScannerOutput?.onScannerDisconnected() }
        }
    }
}