package com.independentpostools.viteposwrapper
/**
 * VitePOS Android Wrapper
 * Version control: 0.2.0 | 2026-10-03
 * Native Epson-compatible ESC/POS printing over LAN raw TCP.
 * Primary target: Epson TM-m30II, editable IP, default port 9100.
 *
 * Important: there is deliberately no automatic retry after a socket/write error.
 * A receipt may have reached the printer even when a response is uncertain.
 */
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset

data class PrintResult(val ok: Boolean, val message: String)

class PrinterService(private val prefs: AppPrefs) {
    fun status(): String = "Epson LAN: ${prefs.printerIp}:${prefs.printerPort}"

    fun print(text: String): PrintResult {
        if (text.isBlank()) return PrintResult(false, "Receipt is empty; nothing printed.")
        return try {
            printLan(receiptBytes(text))
        } catch (e: Exception) {
            PrintResult(false, "Printer error: ${e.message ?: e.javaClass.simpleName}. Check the printer before reprinting.")
        }
    }

    private fun receiptBytes(text: String): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x1B, 0x40)) // ESC @ initialize
        out.write(byteArrayOf(0x1B, 0x61, 0x00)) // left align
        // ASCII/UTF-8 text. Epson code-page behaviour depends on printer configuration;
        // normal English/number/barcode receipt text is supported by default.
        out.write(text.replace("\r", "").toByteArray(Charset.forName("UTF-8")))
        out.write("\n\n\n".toByteArray(Charsets.US_ASCII))
        if (prefs.autoCut) out.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00)) // GS V cut
        return out.toByteArray()
    }

    private fun printLan(data: ByteArray): PrintResult {
        val host = prefs.printerIp.trim()
        require(host.isNotBlank()) { "Printer IP is empty" }
        val port = prefs.printerPort.coerceIn(1, 65535)
        Socket().use { socket ->
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(host, port), 3500)
            socket.soTimeout = 3500
            socket.getOutputStream().use { os ->
                os.write(data)
                os.flush()
            }
        }
        return PrintResult(true, "Receipt sent to Epson at $host:$port.")
    }
}
