package com.datalogic.aladdinsdk.model.firmwaretemplate

import com.datalogic.aladdinsdk.util.FWWriteState
import java.nio.ByteBuffer

class FirmwareHostToDeviceTemplate() {
    // Header
    private val startByte = byteArrayOf(0x05)
    private val mapLen = byteArrayOf(0x84.toByte())
    private val fieldLen = byteArrayOf(0x01, 0x02)
    private val ver = byteArrayOf(0x76, 0x65, 0x72)
    private val version = byteArrayOf(0x01)
    private val fieldLen1 = byteArrayOf(0xAD.toByte())
    private val completionNtf = byteArrayOf(0x63, 0x6F, 0x6D, 0x70, 0x6C, 0x65, 0x74, 0x69, 0x6F, 0x6E, 0x4E, 0x74, 0x66)
    private val answerID = byteArrayOf(0x07)
    private val fieldLen2 = byteArrayOf(0xA3.toByte())
    private val buf = byteArrayOf(0x62, 0x75, 0x66)
    val listOfHeader = listOf(
        startByte,
        mapLen,
        fieldLen,
        ver,
        version,
        fieldLen1,
        completionNtf,
        answerID,
        fieldLen2,
        buf
    )

    //Footer
    private val footerfieldLen = byteArrayOf(0xA3.toByte())
    private val footerntf = byteArrayOf(0x6E.toByte(), 0x74.toByte(), 0x66.toByte())
    private val footerRequestID = byteArrayOf(0x06.toByte())
    private val listOfFooter = listOf(
        footerfieldLen,
        footerntf,
        footerRequestID
    )
fun Int.toByteArray():ByteArray
{
    val buffer=ByteBuffer.allocate(Int.SIZE_BYTES)
    buffer.putInt(this)
    return  buffer.array()
}
    fun getserializedCommand(state: FWWriteState,payloadData:ByteArray): ByteArray {
        val sizeByte=payloadData.size.toByteArray()
        when (state) {
            FWWriteState.PIDNUMBER -> {  //Payload for pidNumber
                val pidNumberDataLenInfo = byteArrayOf(0x10, 0x20)
                val pidNumberDataLen =sizeByte
                val pidNumberDataBuffer = payloadData

                val pidNumberfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(pidNumberDataLenInfo, pidNumberDataLen, pidNumberDataBuffer),
                    listOfFooter
                )
                return pidNumberfirmwareCommand.serialize()
            }
            FWWriteState.SECTIONID -> {
                //Payload for sectionID

                val sectionIDDataLenInfo = byteArrayOf(0x10, 0x20)
                val sectionIDDataLen = sizeByte
                val sectionIDDataBuffer = payloadData

                val sectionIDfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(sectionIDDataLenInfo, sectionIDDataLen, sectionIDDataBuffer),
                    listOfFooter
                )

                return sectionIDfirmwareCommand.serialize()
            }
            FWWriteState.FWVERSION -> {
                //Payload for FWVersion

                val FWVersionDataLenInfo = byteArrayOf(0x10, 0x20)
                val FWVersionDataLen = sizeByte
                val FWVersionDataBuffer = payloadData

                val fwVersionfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(FWVersionDataLenInfo, FWVersionDataLen, FWVersionDataBuffer),
                    listOfFooter
                )
                return fwVersionfirmwareCommand.serialize()
            }
            FWWriteState.FWDATA -> {
                //Payload for FWData


                val fwDataLenInfo = byteArrayOf(0x10, 0x20)
                val fwDataLen =sizeByte
                val fwDataBuffer = payloadData

                val fwfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(fwDataLenInfo, fwDataLen, fwDataBuffer),
                    listOfFooter
                )
                return fwfirmwareCommand.serialize()
            }
            FWWriteState.FWTRANSFERRED -> {
                //Payload for FW transferred

                val fwtransferredDataLenInfo = byteArrayOf(0x10, 0x20)
                val fwtransferredDataLen = sizeByte
                val fwtransferredDataBuffer = payloadData

                val fwtransferredfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(fwtransferredDataLenInfo, fwtransferredDataLen, fwtransferredDataBuffer),
                    listOfFooter
                )

                return fwtransferredfirmwareCommand.serialize()

            }
            FWWriteState.FWREJECTED -> {
                //Payload for FW rejected

                val fwrejectedDataLenInfo = byteArrayOf(0x10, 0x20)
                val fwrejectedDataLen = sizeByte
                val fwrejectedDataBuffer =payloadData

                val fwrejectedfirmwareCommand = HeaderFooterPayloadByte(
                    listOfHeader,
                    listOf(fwrejectedDataLenInfo, fwrejectedDataLen, fwrejectedDataBuffer),
                    listOfFooter
                )
                return fwrejectedfirmwareCommand.serialize()
            }

        }
    }


}