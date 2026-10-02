package com.independentpostools.viteposwrapper
/**
 * VitePOS Android Wrapper
 * Version control: 0.2.0 | 2026-10-03 | Device-local POS, scanner and Epson settings.
 */
import android.content.Context

class AppPrefs(context: Context) {
    private val p = context.getSharedPreferences("vitepos_wrapper", Context.MODE_PRIVATE)

    var posUrl: String
        get() = p.getString("pos_url", "") ?: ""
        set(v) = p.edit().putString("pos_url", v.trim()).apply()

    var printerIp: String
        get() = p.getString("printer_ip", "192.168.1.175") ?: "192.168.1.175"
        set(v) = p.edit().putString("printer_ip", v.trim()).apply()

    var printerPort: Int
        get() = p.getInt("printer_port", 9100)
        set(v) = p.edit().putInt("printer_port", v).apply()

    var autoCut: Boolean
        get() = p.getBoolean("auto_cut", true)
        set(v) = p.edit().putBoolean("auto_cut", v).apply()

    var captureScanner: Boolean
        get() = p.getBoolean("capture_scanner", true)
        set(v) = p.edit().putBoolean("capture_scanner", v).apply()

    /** Stable Android input-device descriptor selected in Hardware Setup. */
    var scannerDescriptor: String
        get() = p.getString("scanner_descriptor", "") ?: ""
        set(v) = p.edit().putString("scanner_descriptor", v).apply()

    var scannerLabel: String
        get() = p.getString("scanner_label", "") ?: ""
        set(v) = p.edit().putString("scanner_label", v).apply()
}
