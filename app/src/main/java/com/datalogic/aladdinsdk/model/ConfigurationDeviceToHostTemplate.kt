package com.datalogic.aladdinsdk.model

import com.datalogic.aladdinsdk.constants.BLEConstants
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.COMMA
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.COMMAND_SUCCESS
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DATA_LEN_INFO_LONG_LENGTH
import com.datalogic.aladdinsdk.constants.BLEConstants.Companion.DATA_LEN_INFO_SHORT_LENGTH
import com.datalogic.aladdinsdk.util.BleUtils
import com.datalogic.aladdinsdk.util.LogUtils
import java.nio.ByteBuffer

class ConfigurationDeviceToHostTemplate(private val byteArray: ByteArray) {

    private val dataLenInfoIndex = 26

    fun isConfigurationCommandSuccess() : Boolean {
        val dataBuffer = getDataBuffer()
        return dataBuffer.isNotEmpty() && !dataBuffer.contains(BLEConstants.COMMAND_FAILED) && !dataBuffer.contains(
            BLEConstants.COMMAND_UNKNOWN
        )
    }

    fun getDataBuffer() : String {
        val tempByteArr = byteArray
        val dataLenInfo = tempByteArr[dataLenInfoIndex]
        var dataBufferLen = 0
        var dataBufferIndex = dataLenInfoIndex
        when (dataLenInfo) {
            DATA_LEN_INFO_SHORT_LENGTH.toByte() -> {
                dataBufferLen = tempByteArr[dataLenInfoIndex + 1].toInt()
                dataBufferIndex = dataLenInfoIndex + 2
            }
            DATA_LEN_INFO_LONG_LENGTH.toByte() -> {
                val dataLenByteArr = byteArrayOf(tempByteArr[dataLenInfo + 1],
                    tempByteArr[dataLenInfoIndex + 2])
                dataBufferLen = ByteBuffer.wrap(dataLenByteArr).int
                dataBufferIndex = dataLenInfoIndex + 3
            }
            else -> {
                LogUtils.error("ConfigurationDeviceToHostTemplate, data len is empty")
            }
        }
        var dataBufferStr = BLEConstants.EMPTY_STRING
        if (dataBufferLen > 0 && dataBufferIndex > dataLenInfoIndex) {
            var dataBufArray = ByteArray(dataBufferLen)
            for (i in dataBufferIndex..dataBufferIndex+(dataBufferLen -1)) {
                dataBufArray += tempByteArr[i]
            }
            dataBufferStr = BleUtils.convertByteArrayToAscii(dataBufArray)
        }
        return dataBufferStr
    }

    fun getConfigurationDataFromBuffer(dataBuffer: String) : Configuration? {
        var configuration = Configuration()
        val dataBuffArr = dataBuffer.split(COMMA)
        val bufWithResponse = dataBuffArr[1]
        if (bufWithResponse == COMMAND_SUCCESS) {
            return null
        } else {
            val dataBufferWithConfig = bufWithResponse.substring(1).split(COMMA)
            for (i in 0..dataBufferWithConfig.lastIndex) {
                val currentConfig = dataBufferWithConfig[i]
                when (currentConfig.substring(0, currentConfig.lastIndex - 2)) {
                    BLEConstants.ConfigurationFeature.VOLUME.featureId -> {
                        configuration.setVolume(currentConfig.substring(currentConfig.lastIndex -1).toInt())
                    }
                }
            }
        }
        return configuration
    }
}