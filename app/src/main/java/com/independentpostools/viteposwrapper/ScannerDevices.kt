package com.independentpostools.viteposwrapper

/**
 * VitePOS Android Wrapper
 * ScannerDevices
 * Version control: 0.2.3 | 2026-10-03
 *
 * Enumerates external non-virtual keyboard/HID devices such as USB barcode scanners.
 */
import android.view.InputDevice

object ScannerDevices {
    data class Device(
        val id: Int,
        val descriptor: String,
        val label: String
    )

    fun list(): List<Device> {
        return InputDevice.getDeviceIds()
            .toList()
            .mapNotNull { id: Int ->
                val device = InputDevice.getDevice(id) ?: return@mapNotNull null

                val isKeyboard =
                    (device.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD

                if (!isKeyboard || device.isVirtual) {
                    return@mapNotNull null
                }

                val label = buildString {
                    append(device.name.ifBlank { "External keyboard/HID" })
                    if (device.vendorId != 0 || device.productId != 0) {
                        append("  [${device.vendorId}:${device.productId}]")
                    }
                }

                Device(
                    id = id,
                    descriptor = device.descriptor,
                    label = label
                )
            }
            .distinctBy { device: Device -> device.descriptor }
    }

    fun matches(eventDevice: InputDevice?, selectedDescriptor: String): Boolean {
        if (eventDevice == null || eventDevice.isVirtual || selectedDescriptor.isBlank()) {
            return false
        }
        return eventDevice.descriptor == selectedDescriptor
    }
}
