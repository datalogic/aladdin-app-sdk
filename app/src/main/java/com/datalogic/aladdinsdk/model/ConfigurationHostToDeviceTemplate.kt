package com.datalogic.aladdinsdk.model

import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.CONFIGURATION_MESSAGE_TX_COMPLETE
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.REQUEST_CONFIGURATION_COMMAND
import com.datalogic.aladdinsdk.util.BleUtils

/**
 * This class represents template for sending configuration from host to device
 */
class ConfigurationHostToDeviceTemplate(private val payload: String) {
    private val totalConfByteArrSize = 19
    private val headerFixedSize = 11
    private val payloadFixedSize = 3
    private val footerFixedSize = 5
    private val startByte = 0x05
    private val mapLen = 0x83
    private val verFieldLen = 0xA3
    private val ver = byteArrayOf(0x76, 0x65, 0x72)
    private val version = 0x01
    private val bufFieldLen = 0xA3
    private val buf = byteArrayOf(0x62, 0x75, 0x66)
    private val ntfFieldLen = 0xA3
    private val ntf = byteArrayOf(0x6E, 0x74, 0x66)

    //TODO: check below payload field
    private val dataLenInfo = 0xC4
    private val dataLen = 0x12

    fun getConfigurationSendingTemplate(): ByteArray {
        var confByteArr = ByteArray(totalConfByteArrSize)
        confByteArr += constructHeader()
        confByteArr += constructPayload()
        confByteArr += constructFooter()
        return confByteArr
    }

    private fun constructHeader(): ByteArray {
        var headerArr = ByteArray(headerFixedSize)
        headerArr += startByte.toByte()
        headerArr += mapLen.toByte()
        headerArr += verFieldLen.toByte()
        headerArr += ver
        headerArr += version.toByte()
        headerArr += bufFieldLen.toByte()
        headerArr += buf
        return headerArr
    }

    private fun constructPayload(): ByteArray {
        var payloadArr = ByteArray(payloadFixedSize)
        payloadArr += dataLenInfo.toByte()
        payloadArr += dataLen.toByte()
        val confArr = BleUtils.convertAsciiToByteArray(payload)
        payloadArr += confArr
        return payloadArr
    }

    private fun constructFooter(): ByteArray {
        var footerArr = ByteArray(footerFixedSize)
        footerArr += ntfFieldLen.toByte()
        footerArr += ntf
        footerArr += if (payload.isNotEmpty()) {
            REQUEST_CONFIGURATION_COMMAND.toByte()
        } else {
            CONFIGURATION_MESSAGE_TX_COMPLETE.toByte()
        }
        return footerArr
    }
}