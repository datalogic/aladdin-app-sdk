package com.datalogic.aladdinsdk.ble

import com.datalogic.aladdinsdk.constants.BLEConstants
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.BATTERY_MANAGEMENT_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONNECTED
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DEVICE_INFO_UUID
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DISCONNECTED
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.isShowDummy
import com.datalogic.aladdinsdk.listener.*
import com.datalogic.aladdinsdk.model.*
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
    private var configurationAckCallback: IConfigurationAck? = null
    private var connectionDisposable: Disposable? = null
    private var isConfigBulkSend = false

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
    fun sendData(dataByte: ByteArray, characteristic: UUID): Completable {
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
    fun getBatteryManagementInfo(): BatteryManagementProfile? {
        if (!isShowDummy) {
            BATTERY_MANAGEMENT_UUID?.let {
                sendData(
                    byteArrayOf(0), BATTERY_MANAGEMENT_UUID
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
    fun getDeviceInfo(): DeviceInfo? {
        if (!isShowDummy) {
            DEVICE_INFO_UUID?.let { sendData(byteArrayOf(0), DEVICE_INFO_UUID) }
        } else {
            return DeviceInfo.getDummyDeviceInfoData()
        }
        return null
    }

    /**
     * Send configuration scanned barcode from aladdin app to hand scanner
     * @param barcodeDetails barcode content
     * @param confAckCallback IConfigurationAck callback
     */
    fun sendConfigurationScannedData(
        barcodeDetails: String, confAckCallback: IConfigurationAck
    ) {
        if (!isShowDummy) {
            isConfigBulkSend = true
            configurationAckCallback = confAckCallback
            val byteArrForWrite =
                ConfigurationHostToDeviceTemplate(barcodeDetails).getConfigurationSendingTemplate()
            sendData(byteArrForWrite, BLEConstants.INPUT_SEGMENT_CHARACTERISTIC_UUID)
        }
    }

    fun writeConfigurationValues(configCommand: String, configCallback: IConfigurationInterface) {
        if (!isShowDummy) {
            configurationCallback = configCallback
            val byteArrForWrite =
                ConfigurationHostToDeviceTemplate(configCommand).getConfigurationSendingTemplate()
            sendData(byteArrForWrite, BLEConstants.INPUT_SEGMENT_CHARACTERISTIC_UUID)
        }
    }

    fun startFirmwareUpgrade(firmwareData: ByteArray, firmwareAck: IFirmwareAck) {
        if (!isShowDummy) {
            //TODO: Need to integrate firmware upgrade
        }
    }

    private fun readCharacteristicData(byteArray: ByteArray) {
        val requestId = byteArray[byteArray.size - BLEConstants.NUMERIC_1]
        when (requestId.toInt()) {
            BLEConstants.ANSWER_CONFIGURATION_COMMAND -> {
                if (isConfigBulkSend) {
                    if (ConfigurationDeviceToHostTemplate(byteArray).isConfigurationCommandSuccess()) {
                        configurationAckCallback!!.onConfigurationDataReceivedSuccessfully()
                        sendConfigurationScannedData(
                            BLEConstants.EMPTY_STRING,
                            configurationAckCallback!!
                        )
                    } else {
                        configurationAckCallback!!.onConfigurationDataReceivedUnSuccessful()
                    }
                    isConfigBulkSend = false
                } else {
                    val template = ConfigurationDeviceToHostTemplate(byteArray)
                    if (template.isConfigurationCommandSuccess()) {
                        val configuration = template.getConfigurationDataFromBuffer(template.getDataBuffer())
                        if (configuration == null) {
                            configurationCallback!!.onConfigurationWriteSuccessfull()
                        } else {
                            configurationCallback!!.onConfigurationDataReceived(configuration)
                        }
                    } else {
                        configurationCallback!!.onConfigurationWriteUnsuccessfull()
                    }
                }
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