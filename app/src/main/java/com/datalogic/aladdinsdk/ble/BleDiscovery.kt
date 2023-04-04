package com.datalogic.aladdinsdk.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class BleDiscovery(
    private val qrContentScanned: String,
    private val context: Context
) {
    private lateinit var advertisementMatcherStr: String
    private lateinit var mContext: Context
    private lateinit var mRxBleClient: RxBleClient
    private var scanDisposable: Disposable? = null
    private val protectionLock = ReentrantLock(true)
    private var isScanning = false

    fun scanBleDevice() {
        protectionLock.withLock {
            if (!isScanning) {
                isScanning = true
                if (scanDisposable == null) {
                    scanDisposable = mRxBleClient.scanBleDevices(
                        ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build()
                        // add filters if needed
                    ).retryWhen { error ->
                        error.flatMap {
                            isScanning = false;
                            mRxBleClient.observeStateChanges()
                        }
                            .filter { it == RxBleClient.State.READY }
                            .doOnNext { }
                    }.subscribe({
                        this.onBLEScan(it)
                    }, {})
                }
            }
        }
    }

    private fun initRxBleClient() {
        advertisementMatcherStr = qrContentScanned
        mContext = context
        mRxBleClient = RxBleClient.create(mContext)
    }

    private fun onBLEScan(result: ScanResult): RxBleDevice? {
        if (isScanning) {
            val advData = result.scanRecord.bytes
            if (parseAdvertisementData(advData)) {
                BleConnection.init(result.bleDevice)
                BleConnection.establishConnection(context)
                return result.bleDevice
            }
        }
        return null
    }

    private fun parseAdvertisementData(advValue: ByteArray): Boolean {
        //TODO : parse advertisement logic
        return true
    }
}