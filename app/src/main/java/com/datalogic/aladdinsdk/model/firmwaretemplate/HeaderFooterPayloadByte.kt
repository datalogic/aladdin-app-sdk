package com.datalogic.aladdinsdk.model.firmwaretemplate

class HeaderFooterPayloadByte(
    val headerSections: List<ByteArray>,
    val payload: List<ByteArray>,
    val footerSections: List<ByteArray>
) {

    fun serialize(): ByteArray {
        val commandSize = getTotalSize()
        val serializedCommand = ByteArray(commandSize)

        var currentIndex = 0

        // Serialize header sections
        for (section in headerSections) {
            System.arraycopy(section, 0, serializedCommand, currentIndex, section.size)
            currentIndex += section.size
        }

        // Serialize data sections
        for (section in payload) {
            System.arraycopy(section, 0, serializedCommand, currentIndex, section.size)
            currentIndex += section.size
        }

        // Serialize footer sections
        for (section in footerSections) {
            System.arraycopy(section, 0, serializedCommand, currentIndex, section.size)
            currentIndex += section.size
        }

        return serializedCommand
    }

    private fun getTotalSize(): Int {
        var totalSize = 0

        // Calculate total size of header sections
        for (section in headerSections) {
            totalSize += section.size
        }

        // Calculate total size of data sections
        for (section in payload) {
            totalSize += section.size
        }

        // Calculate total size of footer sections
        for (section in footerSections) {
            totalSize += section.size
        }

        return totalSize
    }
}
