package com.independentpostools.viteposwrapper
/**
 * VitePOS Android Wrapper
 * Version control: 0.2.0 | 2026-10-03 | Hardware Setup UI.
 */
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: AppPrefs
    private var scannerDevices: List<ScannerDevices.Device> = emptyList()
    private val testBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPrefs(this)
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30, 24, 30, 36) }
        fun title(t: String) = TextView(this).apply { text=t; textSize=21f; setPadding(0,22,0,8) }
        fun label(t: String) = TextView(this).apply { text=t; textSize=15f; setPadding(0,14,0,5) }

        box.addView(TextView(this).apply { text="VitePOS Hardware Setup"; textSize=26f })
        box.addView(TextView(this).apply { text="Android Wrapper v0.2.0"; textSize=13f })

        val url = EditText(this).apply { hint="https://your-site.com/pos"; setText(prefs.posUrl) }
        box.addView(title("VitePOS")); box.addView(label("POS URL")); box.addView(url)

        box.addView(title("Barcode Scanner"))
        val scannerEnabled = CheckBox(this).apply { text="Use USB/HID barcode scanner"; isChecked=prefs.captureScanner }
        box.addView(scannerEnabled)
        scannerDevices = ScannerDevices.list()
        val choices = mutableListOf("Select USB barcode scanner…")
        choices.addAll(scannerDevices.map { it.label })
        val scannerSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, choices)
            val idx = scannerDevices.indexOfFirst { it.descriptor == prefs.scannerDescriptor }
            setSelection(if (idx >= 0) idx + 1 else 0)
        }
        box.addView(scannerSpinner)
        val scannerStatus = TextView(this).apply {
            text = if (scannerDevices.isEmpty()) "No external USB/HID keyboard device detected. Connect the scanner through USB/OTG, then reopen Hardware Setup."
                   else "Select the scanner, then scan a barcode below."
            setPadding(0,8,0,8)
        }
        box.addView(scannerStatus)
        val scanTest = EditText(this).apply {
            hint="Tap here, then scan a barcode to test"
            isFocusableInTouchMode=true
        }
        box.addView(scanTest)

        box.addView(title("Receipt Printer"))
        box.addView(TextView(this).apply { text="Epson TM-m30II — LAN / IP printing"; textSize=16f })
        val ip = EditText(this).apply { setText(prefs.printerIp); hint="192.168.1.175" }
        val port = EditText(this).apply { setText(prefs.printerPort.toString()); inputType=2 }
        val cut = CheckBox(this).apply { text="Cut paper after receipt"; isChecked=prefs.autoCut }
        box.addView(label("Printer IP / hostname")); box.addView(ip)
        box.addView(label("Raw print port")); box.addView(port); box.addView(cut)
        val printerStatus = TextView(this).apply { text="Printer is sent raw ESC/POS directly. No Android/A4 print dialog."; setPadding(0,8,0,8) }
        box.addView(printerStatus)
        box.addView(Button(this).apply {
            text="TEST PRINT"
            setOnClickListener {
                saveHardware(scannerEnabled, scannerSpinner, ip, port, cut)
                printerStatus.text="Sending test receipt…"
                Thread {
                    val r=PrinterService(prefs).print("VitePOS Android Wrapper\nVersion 0.2.0\nEpson LAN test\n\nBarcode scanner + receipt printer setup")
                    runOnUiThread { printerStatus.text=r.message }
                }.start()
            }
        })

        box.addView(Button(this).apply {
            text="SAVE AND OPEN POS"
            setOnClickListener {
                val u=url.text.toString().trim()
                if (u.isNotBlank() && !(u.startsWith("https://") || u.startsWith("http://"))) {
                    Toast.makeText(this@SettingsActivity,"POS URL must start with https:// or http://",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                prefs.posUrl=u
                saveHardware(scannerEnabled, scannerSpinner, ip, port, cut)
                Toast.makeText(this@SettingsActivity,"Hardware settings saved.",Toast.LENGTH_SHORT).show()
                finish()
            }
        })
        scroll.addView(box); setContentView(scroll)

        scanTest.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener true
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                val code=testBuffer.toString().trim(); testBuffer.clear()
                if(code.isNotEmpty()) { scanTest.setText(code); scanTest.setSelection(scanTest.text.length); scannerStatus.text="Scanner test OK: $code" }
                true
            } else {
                val c=event.unicodeChar
                if(c>0){testBuffer.append(c.toChar()); true}else false
            }
        }
    }

    private fun saveHardware(scannerEnabled: CheckBox, scannerSpinner: Spinner, ip: EditText, port: EditText, cut: CheckBox) {
        prefs.captureScanner=scannerEnabled.isChecked
        val idx=scannerSpinner.selectedItemPosition-1
        if(idx in scannerDevices.indices){ prefs.scannerDescriptor=scannerDevices[idx].descriptor; prefs.scannerLabel=scannerDevices[idx].label }
        prefs.printerIp=ip.text.toString().trim()
        prefs.printerPort=port.text.toString().toIntOrNull()?.coerceIn(1,65535) ?: 9100
        prefs.autoCut=cut.isChecked
    }
}
