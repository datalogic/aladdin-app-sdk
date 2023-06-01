package com.datalogic.aladdinsdk.ble

import android.content.Context
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.BATTERY_MANAGEMENT_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_SCANNED_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONNECTED
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DEVICE_INFO_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DISCONNECTED
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.isShowDummy
import com.datalogic.aladdinsdk.listener.IBatteryManagementInterface
import com.datalogic.aladdinsdk.listener.IConfigurationInterface
import com.datalogic.aladdinsdk.listener.IDeviceInfoInterface
import com.datalogic.aladdinsdk.model.BatteryManagementProfile
import com.datalogic.aladdinsdk.model.Configuration
import com.datalogic.aladdinsdk.model.DeviceInfo
import com.datalogic.aladdinsdk.util.LogUtils
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.polidea.rxandroidble3.exceptions.BleGattCharacteristicException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

/**
 * This class use for connecting hand scanner with android device
 */
object BleConnection {
    private var mCurrentConnectedDevice: RxBleDevice? = null
    private var bleConnection: RxBleConnection? = null
    private val protectionLock = ReentrantLock(true)
    private lateinit var batteryManagementCallBack: IBatteryManagementInterface
    private lateinit var deviceInfoCallback: IDeviceInfoInterface
    private lateinit var configurationCallback: IConfigurationInterface
    private var connectionDisposable: Disposable? = null

    /**
     * establish connection between hand scanner and aladdin app
     * @param bleDevice RxBleDevice device selected by user
     */
    fun connect(bleDevice: RxBleDevice) {
        bleDevice.establishConnection(false).doFinally { dispose() }.subscribe({
                onConnectionReceived()
                mCurrentConnectedDevice = bleDevice
            }, {
                onConnectionFailure(it)
            }).let { connectionDisposable = it }
    }

    private fun onConnectionReceived() {
        LogUtils.debug("connection received")
        ConnectionState.setCurrentState(CONNECTED)
    }

    private fun onConnectionFailure(error: Throwable) {
        if (error is BleDisconnectedException) {
            LogUtils.debug("Connection lost. $error")
        } else if (error is TimeoutException) {
            LogUtils.debug("Time out. $error")
        }
    }

    fun disconnectConnection() {
        LogUtils.debug("disconnecting ble connection")
        connectionDisposable?.dispose()
        ConnectionState.setCurrentState(DISCONNECTED)
        mCurrentConnectedDevice = null
    }

    fun getCurrentConnectedDevice(): RxBleDevice? {
        return mCurrentConnectedDevice
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
                        bleConnection!!.writeCharacteristic(characteristic, dataByte)
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

    /**
     * disconnect BLE connection
     */
    private fun disconnect() {
        dispose()
    }

    /**
     * dispose BLE connection
     */
    private fun dispose() {
        connectionDisposable = null
        ConnectionState.setCurrentState(DISCONNECTED)
        mCurrentConnectedDevice = null
    }
}