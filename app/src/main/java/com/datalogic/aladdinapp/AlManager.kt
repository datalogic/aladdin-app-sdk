package com.datalogic.aladdinapp

import android.content.ServiceConnection
import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import com.datalogic.aladdinapp.interfaces.IScannerOutput
import com.datalogic.aladdinapp.interfaces.IServiceOutput
import kotlin.Throws

open class AlManager(private val context: Context) {
    var isConnectedToService = false
        private set
    private var myService: IMyAidlInterface? = null
    private var iServiceOutput: IServiceOutput? = null
    private var iScannerOutput: IScannerOutput? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            myService = IMyAidlInterface.Stub.asInterface(service)
            isConnectedToService = true
            if (iServiceOutput != null) iServiceOutput!!.onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            if (iServiceOutput != null) iServiceOutput!!.onServiceDisconnected()
            isConnectedToService = false
        }
    }

    fun ensureConnectionToService(): Boolean {
        if (!isConnectedToService) {
            val intent = Intent()
            intent.setClassName(
                "com.datalogic.aladdin", "com.datalogic.aladdinapp.data.model.EndlessService"
            )
            isConnectedToService = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        return isConnectedToService
    }

    fun unbindService() {
        context.unbindService(connection)
    }

    val isConnectedToScanner: Boolean
        get() {
            try {
                return if (myService != null) myService!!.isConnectedToScanner else false
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return false
        }

    fun subscribeToScans(scannerOutput: IScannerOutput?) {
        iScannerOutput = scannerOutput
        try {
            if (myService != null) myService!!.SubscribeScans(iScannerServiceCallback)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun subscribeToServiceEvents(serviceOutput: IServiceOutput?) {
        iServiceOutput = serviceOutput
    }

    fun unsubscribeFromScans() {
        iScannerOutput = null
        try {
            myService!!.UnsubscribeScans(iScannerServiceCallback)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun unsubscribeFromServiceEvents() {
        iServiceOutput = null

    }

    fun getLatestBarcodeData(): String {
        try {
            return myService!!.qrCode
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return "Default Data"
    }

    var iScannerServiceCallback: IRemoteServiceCallback = object : IRemoteServiceCallback.Stub() {
        @Throws(RemoteException::class)
        override fun onBarcodeScanned(message: String) {
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onBarcodeScanned(message) }
        }

        @Throws(RemoteException::class)
        override fun onScannerConnected() {
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onScannerConnected() }
        }

        @Throws(RemoteException::class)
        override fun onScannerDisconnected() {
            val mainHandler = Handler(Looper.getMainLooper());
            mainHandler.post { iScannerOutput!!.onScannerDisconnected() }
        }
    }
}