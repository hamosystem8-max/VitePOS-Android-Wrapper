package com.independentpostools.viteposwrapper
/** Version control: 0.2.0 | 2026-10-03 | External USB/HID scanner enumeration. */
import android.view.InputDevice

object ScannerDevices {
    data class Device(val id: Int, val descriptor: String, val label: String)

    fun list(): List<Device> = InputDevice.getDeviceIds().mapNotNull { id ->
        val d = InputDevice.getDevice(id) ?: return@mapNotNull null
        val keyboard = (d.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
        if (!keyboard || d.isVirtual) return@mapNotNull null
        val label = buildString {
            append(d.name.ifBlank { "External keyboard/HID" })
            if (d.vendorId != 0 || d.productId != 0) append("  [${d.vendorId}:${d.productId}]")
        }
        Device(id, d.descriptor ?: "device-$id", label)
    }.distinctBy { it.descriptor }

    fun matches(eventDevice: InputDevice?, selectedDescriptor: String): Boolean {
        if (eventDevice == null || eventDevice.isVirtual || selectedDescriptor.isBlank()) return false
        return eventDevice.descriptor == selectedDescriptor
    }
}
