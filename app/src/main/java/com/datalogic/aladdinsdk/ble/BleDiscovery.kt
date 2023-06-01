package com.datalogic.aladdinsdk.ble

import android.content.Context
import com.datalogic.aladdinsdk.listener.IScannedDeviceList
import com.datalogic.aladdinsdk.util.LogUtils
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.exceptions.BleScanException
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * This class use for scanning and discovery of hand scanner
 */
class BleDiscovery(private val context: Context) {
    private lateinit var mContext: Context
    private lateinit var mRxBleClient: RxBleClient
    private lateinit var deviceScanListener: IScannedDeviceList
    private var scanDisposable: Disposable? = null
    private val protectionLock = ReentrantLock(true)
    private var isScanning = false

    /**
     * Init BLE client
     */
    private fun initRxBleClient() {
        mContext = context
        mRxBleClient = RxBleClient.create(mContext)
    }

    /**
     * Scan BLE device near by
     */
    fun scanBleDevice(listener: IScannedDeviceList) {
        initRxBleClient()
        deviceScanListener = listener
        protectionLock.withLock {
            if (!isScanning) {
                isScanning = true
                if (scanDisposable == null) {
                    scanDisposable = mRxBleClient.scanBleDevices(
                        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build()
                    ).subscribe({
                        this.onBLEScan(it)
                    }, {
                        onScanFailure(it)
                    })
                }
            }
        }
    }

    private fun onScanFailure(error: Throwable) {
        if (error is BleScanException) {
            LogUtils.debug("scan failed, $error")
        }
        isScanning = false
    }

    /**
     * Callback after scanning nearby devices
     */
    private fun onBLEScan(result: ScanResult) {
        if (isScanning) {
            val device = result.bleDevice
            if (device != null && !device.name.isNullOrEmpty() && this::deviceScanListener.isInitialized) {
                LogUtils.debug("available device: $device")
                deviceScanListener.onDeviceAvailable(device)
            }
        }
    }

    fun stopScan() {
        scanDisposable?.dispose()
    }
}