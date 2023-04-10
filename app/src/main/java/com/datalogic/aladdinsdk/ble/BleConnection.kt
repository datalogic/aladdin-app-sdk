package com.datalogic.aladdinsdk.ble

import android.content.Context
import com.datalogic.aladdinsdk.constants.BLEConstants
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.BATTERY_MANAGEMENT_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_SCANNED_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DEVICE_INFO_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.isShowDummy
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

/**
 * This class use for connecting hand scanner with android device
 */
object BleConnection {
    private lateinit var mRxBleDevice: RxBleDevice
    private var connectionSubscription = CompositeDisposable()
    private var bleConnection: RxBleConnection? = null
    private var connectionState = "disconnected"
    private val protectionLock = ReentrantLock(true)
    private lateinit var batteryManagementCallBack: IBatteryManagementInterface
    private lateinit var deviceInfoCallback: IDeviceInfoInterface
    private lateinit var configurationCallback: IConfigurationInterface

    /**
     * establish connection between hand scanner and aladdin app
     * @param context Context
     */
    fun establishConnection(context: Context) {
        if (this::mRxBleDevice.isInitialized) {
            establishConnection(mRxBleDevice, context)
        }
    }

    /**
     * Initialize Ble device for connection
     * @param rxBleDevice BLE device
     */
    fun init(rxBleDevice: RxBleDevice) {
        mRxBleDevice = rxBleDevice
    }

    /**
     * set callback for getting battery information into aladdin app
     * @param callback Battery management Interface
     */
    fun setCallBackForBatteryManagement(callback: IBatteryManagementInterface) {
        batteryManagementCallBack = callback
    }

    /**
     * Request for battery information to hand scanner
     * @param context Context
     */
    fun getBatteryManagementInfo(context: Context): BatteryManagementProfile? {
        if (!isShowDummy) {
            BATTERY_MANAGEMENT_UUID?.let {
                sendData(
                    byteArrayOf(0), BATTERY_MANAGEMENT_UUID, context
                )
            }
        } else {
            return BatteryManagementProfile.getDummyBatteryManagementProfileData()
        }
        return null
    }

    /**
     * set callback for getting device information into aladdin app
     * @param callback device management Interface
     */
    fun setCallBackForDeviceInfo(callback: IDeviceInfoInterface) {
        deviceInfoCallback = callback
    }

    /**
     * Request for device information to hand scanner
     * @param context Context
     */
    fun getDeviceInfo(context: Context): DeviceInfo? {
        if (!isShowDummy) {
            DEVICE_INFO_UUID?.let { sendData(byteArrayOf(0), DEVICE_INFO_UUID, context) }
        } else {
            return DeviceInfo.getDummyDeviceInfoData()
        }
        return null
    }

    /**
     * set callback for getting configuration information into aladdin app
     * @param callback configuration management Interface
     */
    fun setCallBackForConfiguration(callback: IConfigurationInterface) {
        configurationCallback = callback
    }

    /**
     * Request for configuration information to hand scanner
     * @param callback Battery management Interface
     */
    fun getConfigurationInfo(context: Context): Configuration? {
        if (!isShowDummy) {
            CONFIGURATION_UUID?.let { sendData(byteArrayOf(0), CONFIGURATION_UUID, context) }
        } else {
            return Configuration.getDummyConfigurationData()
        }
        return null
    }

    /**
     * Send configuration setting changes from aladdin app to hand scanner
     * @param configData Configuration
     * @param context Context
     */
    fun sendConfigurationChangedData(configData: Configuration, context: Context) {
        if (!isShowDummy) {
            CONFIGURATION_UUID?.let { sendData(byteArrayOf(0), CONFIGURATION_UUID, context) }
        }
    }

    /**
     * Send configuration scanned barcode from aladdin app to hand scanner
     * @param callback Battery management Interface
     * @param context Context
     */
    fun sendConfigurationScannedData(barcodeDetails: String, context: Context) {
        if (!isShowDummy) {
            CONFIGURATION_SCANNED_UUID?.let {
                sendData(
                    byteArrayOf(0), CONFIGURATION_SCANNED_UUID, context
                )
            }
        }
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
                        false, Timeout(5000, TimeUnit.MILLISECONDS)
                    ).subscribe({ rxBleConnection ->
                        protectionLock.withLock {
                            bleConnection = rxBleConnection
                            rxBleConnection.setupNotification(
                                BLEConstants.BATTERY_LEVEL_UUID!!, NotificationSetupMode.DEFAULT
                            ).flatMap { observable -> observable }.subscribe({
                                readBatteryLevel(it)
                            }, {
                                LogUtils.error("Subscription error for BATTERY_LEVEL_UUID")
                            })

                            rxBleConnection.setupNotification(
                                BLEConstants.BATTERY_MANAGEMENT_UUID!!,
                                NotificationSetupMode.DEFAULT
                            ).flatMap { observable -> observable }.subscribe({
                                readBatteryManagementProfile(it)
                            }, {
                                LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                            })

                            rxBleConnection.setupNotification(
                                BLEConstants.DEVICE_INFO_UUID!!, NotificationSetupMode.DEFAULT
                            ).flatMap { observable -> observable }.subscribe({
                                readDeviceInfo(it)
                            }, {
                                LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                            })

                            rxBleConnection.setupNotification(
                                BLEConstants.CONFIGURATION_UUID!!, NotificationSetupMode.DEFAULT
                            ).flatMap { observable -> observable }.subscribe({
                                readConfigurationData(it)
                            }, {
                                LogUtils.error("Subscription error for BATTERY_MANAGEMENT_UUID")
                            })

                            bleDevice.observeConnectionStateChanges().subscribe { connectionState ->
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

    /**
     * Send information from aladdin app to hand scanner
     * @param dataByte sending data in form of byte array
     * @param characteristic UUID for the feature
     */
    fun sendData(dataByte: ByteArray, characteristic: UUID, context: Context): Completable {
        return Completable.defer {
            bleConnection!!.writeCharacteristic(characteristic, dataByte)
                .onErrorResumeNext { throwable ->
                    if (throwable is BleGattCharacteristicException) {
                        LogUtils.warn("Failed to send data.")
                        tryReconnection(context).andThen(Single.defer {
                            bleConnection!!.writeCharacteristic(characteristic, dataByte)
                        })
                    } else {
                        Single.error(throwable)
                    }
                }.doOnSuccess {
                    LogUtils.debug("On characteristic written success.")
                }.doOnError { throwable ->
                    if (throwable !is BleCharacteristicNotFoundException) {
                        LogUtils.error("Failed to send data. Drop connection.")
                        disconnect()
                    } else {
                        LogUtils.warn("Failed to send data.")
                    }
                }.ignoreElement()
        }
    }

    private fun readBatteryLevel(value: ByteArray): Int {
        //TODO:parse byte array to read battery level
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
            LogUtils.debug("Attempt reconnection with delay.")
            connectionState = "reconnecting"
            Unit
        }.andThen(clearConnection()).delay(BLEConstants.RECONNECTION_DELAY, TimeUnit.MILLISECONDS)
            .andThen(establishConnection(mRxBleDevice, context))
    }

    /**
     * disconnect BLE connection
     */
    private fun disconnect(): Completable {
        return Completable.defer {
            clearConnection().andThen(Completable.fromCallable {
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