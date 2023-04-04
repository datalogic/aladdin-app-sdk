package com.datalogic.aladdinsdk.ble

import android.content.Context
import android.util.Log
import com.datalogic.aladdinsdk.constants.BLEConstants
import com.datalogic.aladdinsdk.listener.IBatteryManagementInterface
import com.datalogic.aladdinsdk.listener.IConfigurationInterface
import com.datalogic.aladdinsdk.listener.IDeviceInfoInterface
import com.datalogic.aladdinsdk.util.BleUtils
import com.datalogic.aladdinsdk.model.BatteryManagementProfile
import com.datalogic.aladdinsdk.model.Configuration
import com.datalogic.aladdinsdk.model.DeviceInfo
import com.datalogic.aladdinsdk.util.LogUtils
import com.polidea.rxandroidble3.NotificationSetupMode
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.Timeout
import com.polidea.rxandroidble3.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.polidea.rxandroidble3.exceptions.BleGattCharacteristicException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.CompletableSubject
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object BleConnection{
    private lateinit var mRxBleDevice: RxBleDevice
    private var connectionSubscription = CompositeDisposable()
    private var bleConnection: RxBleConnection? = null
    private var connectionState = "disconnected"
    private val protectionLock = ReentrantLock(true)
    private lateinit var batteryManagementCallBack: IBatteryManagementInterface
    private lateinit var deviceInfoCallback: IDeviceInfoInterface
    private lateinit var configurationCallback: IConfigurationInterface

    fun establishConnection(context: Context) {
        if (this::mRxBleDevice.isInitialized) {
            establishConnection(mRxBleDevice, context)
        }
    }

    fun init(rxBleDevice: RxBleDevice) {
        mRxBleDevice = rxBleDevice
    }

    fun setCallBackForBatteryManagement(callback: IBatteryManagementInterface) {
        batteryManagementCallBack = callback
    }

    fun setCallBackForDeviceInfo(callback: IDeviceInfoInterface) {
        deviceInfoCallback = callback
    }

    fun setCallBackForConfiguration(callback: IConfigurationInterface) {
        configurationCallback = callback
    }

    private fun establishConnection(bleDevice: RxBleDevice, context: Context): Completable {
        return Completable.defer {
            if (BleUtils.getBleAdapter(context).isEnabled) {
                Completable.complete()
            } else {
                Completable.error(IllegalStateException("Bluetooth disabled"))
            }
        }.andThen(Completable.fromCallable {
            val connectionSubject = CompletableSubject.create()
            connectionSubscription = CompositeDisposable().apply {
                add(
                    bleDevice.establishConnection(
                        false,
                        Timeout(5000, TimeUnit.MILLISECONDS)
                    ).subscribe({ rxBleConnection ->
                        protectionLock.withLock {
                            bleConnection = rxBleConnection
                            rxBleConnection.setupNotification(
                                BLEConstants.BATTERY_LEVEL_UUID!!,
                                NotificationSetupMode.DEFAULT
                            )
                                .flatMap { observable -> observable }
                                .subscribe(
                                    {
                                        readBatteryLevel(it)
                                    },
                                    {
                                        LogUtils.error("Subscription error for BATTERY_LEVEL_UUID")
                                    })

                            rxBleConnection.setupNotification(
                                BLEConstants.BATTERY_MANAGEMENT_UUID!!,
                                NotificationSetupMode.DEFAULT
                            )
                                .flatMap { observable -> observable }
                                .subscribe(
                                    {
                                        readBatteryManagementProfile(it)
                                    },
                                    {
                                        LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                                    })

                            rxBleConnection.setupNotification(
                                BLEConstants.DEVICE_INFO_UUID!!,
                                NotificationSetupMode.DEFAULT
                            )
                                .flatMap { observable -> observable }
                                .subscribe(
                                    {
                                        readDeviceInfo(it)
                                    },
                                    {
                                        LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                                    })

                            rxBleConnection.setupNotification(
                                BLEConstants.CONFIGURATION_UUID!!,
                                NotificationSetupMode.DEFAULT
                            )
                                .flatMap { observable -> observable }
                                .subscribe(
                                    {
                                        readConfigurationData(it)
                                    },
                                    {
                                        LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                                    })

                            bleDevice.observeConnectionStateChanges()
                                .subscribe { connectionState ->
                                    LogUtils.debug("connectivity state has changed to " + connectionState)
                                    if (connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                                        disconnect().subscribe()
                                        connectionSubscription.clear()
                                    }
                                }
                            connectionState = "connected"
                            connectionSubject.onComplete()
                        }
                    }, { error ->
                        protectionLock.withLock {
                            if (error is BleDisconnectedException) {
                                // isAutoDisconnectAfterConnection = false;
                                LogUtils.debug("Connection lost.")
                            } else if (error is TimeoutException) {
                                //  isAutoDisconnectAfterConnection = true
                                LogUtils.debug("Connection failed.")
                            }
                            //Clear connection info
                            clearConnection().blockingAwait()

                            //Don't report disconnection if we are trying to reconnect
                            if (connectionState == "reconnecting") {
                                if (!connectionSubject.hasComplete()) {
                                    connectionSubject.onError(error)
                                }
                            }
                            connectionState = "disconnected"
                        }
                    })
                )
            }
            connectionSubject.blockingAwait()
        })
    }

    fun sendData(dataByte: ByteArray, characteristic: UUID, context: Context): Completable {
        return Completable.defer {
            bleConnection!!.writeCharacteristic(characteristic, dataByte)
                .onErrorResumeNext { throwable ->
                    if (throwable is BleGattCharacteristicException) {
                        Log.w(BLEConstants.LOG_TAG, "Failed to send data.")
                        tryReconnection(context)
                            .andThen(Single.defer {
                                bleConnection!!.writeCharacteristic(characteristic, dataByte)
                            })
                    } else {
                        Single.error(throwable)
                    }
                }
                .doOnSuccess {
                    Log.d(BLEConstants.LOG_TAG, "On characteristic written success.")
                }.doOnError { throwable ->
                    if (throwable !is BleCharacteristicNotFoundException) {
                        Log.e(BLEConstants.LOG_TAG, "Failed to send data. Drop connection.")
                        disconnect()
                    } else {
                        Log.w(BLEConstants.LOG_TAG, "Failed to send data.")
                    }
                }.ignoreElement()
        }
    }

    private fun readBatteryLevel(value: ByteArray): Int {
        // parse byte array to read battery level
        // currently using dummy value
        return 100
    }

    private fun readBatteryManagementProfile(value: ByteArray) {
        val batteryManagementProfile = BatteryManagementProfile.parseBatteryManagementResult(value)
        if (this::batteryManagementCallBack.isInitialized) {
            batteryManagementCallBack.onBatteryManagementDataReceived(batteryManagementProfile)
        }
    }

    private fun readDeviceInfo(value: ByteArray) {
        val deviceInfo = DeviceInfo.parseDeviceInfo(value)
        if (this::deviceInfoCallback.isInitialized) {
            deviceInfoCallback.onDeviceInfoDataReceived(deviceInfo)
        }
    }

    private fun readConfigurationData(value: ByteArray) {
        val configData = Configuration.parseConfiguration(value)
        if (this::configurationCallback.isInitialized) {
            configurationCallback.onConfigurationDataReceived(configData)
        }
    }

    private fun tryReconnection(context: Context): Completable {
        return Completable.fromCallable {
            Log.d(BLEConstants.LOG_TAG, "Attempt reconnection with delay.")
            connectionState = "reconnecting"
            Unit
        }.andThen(clearConnection())
            .delay(BLEConstants.RECONNECTION_DELAY, TimeUnit.MILLISECONDS)
            .andThen(establishConnection(mRxBleDevice, context))
    }

    private fun disconnect(): Completable {
        return Completable.defer {
            clearConnection()
                .andThen(Completable.fromCallable {
                    connectionState = "disconnected"
                    Unit
                })
        }
    }

    private fun clearConnection(): Completable {
        return Completable.fromCallable {
            try {
                if (!connectionSubscription.isDisposed) {
                    connectionSubscription.dispose()
                } else {
                    LogUtils.debug("connection is already disposed")
                }
            } catch (e: Exception) {
                LogUtils.error("Unexpected error occur while clearing connection")
            }
        }
    }
}