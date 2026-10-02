package com.independentpostools.viteposwrapper
/**
 * VitePOS Android Wrapper
 * Version control: 0.2.0 | 2026-10-03
 * - Trusted WebView wrapper for VitePOS.
 * - Selected USB/HID barcode scanner -> VitePOS barcode event.
 * - Native Epson TM-m30II LAN receipt printing via JavaScript bridge.
 */
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var prefs: AppPrefs
    private val scanBuffer = StringBuilder()
    private var firstScanKeyAt = 0L
    private var lastScanKeyAt = 0L
    private val maxInterKeyMs = 120L
    private val minBarcodeLength = 3

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPrefs(this)
        val root = FrameLayout(this)
        webView = WebView(this)
        root.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val gear = ImageButton(this).apply {
            contentDescription = "Hardware settings"
            setImageResource(android.R.drawable.ic_menu_preferences)
            setBackgroundColor(0xD9FFFFFF.toInt())
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
        root.addView(gear, FrameLayout.LayoutParams(60.dp, 60.dp).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.END
            topMargin = 10.dp
            marginEnd = 10.dp
        })
        setContentView(root)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
        }
        webView.addJavascriptInterface(NativeBridge(), "PosNative")
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val target = request?.url ?: return true
                return if (isTrusted(target)) false else {
                    Toast.makeText(this@MainActivity, "External page blocked inside POS: ${target.host ?: target}", Toast.LENGTH_LONG).show()
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectNativeReady()
            }
        }
        loadConfiguredUrl()
    }

    private fun configuredUri(): Uri? = runCatching { Uri.parse(prefs.posUrl) }.getOrNull()

    private fun isTrusted(uri: Uri): Boolean {
        val base = configuredUri() ?: return false
        val schemeOk = uri.scheme.equals("https", true) || uri.scheme.equals("http", true)
        return schemeOk && !uri.host.isNullOrBlank() && uri.host.equals(base.host, true)
    }

    private fun injectNativeReady() {
        webView.evaluateJavascript("""
            (function(){
              window.__VPE_ANDROID_WRAPPER__={version:'0.2.0',printer:'LAN',scanner:'USB-HID'};
              window.dispatchEvent(new CustomEvent('posnative:ready',{detail:window.__VPE_ANDROID_WRAPPER__}));
            })();
        """.trimIndent(), null)
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized && webView.url == null) loadConfiguredUrl()
    }

    private fun loadConfiguredUrl() {
        val url = prefs.posUrl.trim()
        val parsed = runCatching { Uri.parse(url) }.getOrNull()
        if (url.isBlank() || parsed?.host.isNullOrBlank() || !(url.startsWith("https://") || url.startsWith("http://"))) {
            startActivity(Intent(this, SettingsActivity::class.java))
            Toast.makeText(this, "Set the VitePOS URL first.", Toast.LENGTH_LONG).show()
        } else webView.loadUrl(url)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!prefs.captureScanner || !ScannerDevices.matches(event.device, prefs.scannerDescriptor)) {
            return super.dispatchKeyEvent(event)
        }
        if (event.action != KeyEvent.ACTION_DOWN) return true

        val now = SystemClock.elapsedRealtime()
        if (scanBuffer.isEmpty()) firstScanKeyAt = now
        if (lastScanKeyAt > 0 && now - lastScanKeyAt > maxInterKeyMs) {
            scanBuffer.clear()
            firstScanKeyAt = now
        }
        lastScanKeyAt = now

        if (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            val code = scanBuffer.toString().trim()
            scanBuffer.clear()
            if (code.length >= minBarcodeLength) sendBarcodeToWeb(code)
            return true
        }
        val c = event.unicodeChar
        if (c > 0 && !event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed) {
            scanBuffer.append(c.toChar())
        }
        return true
    }

    private fun sendBarcodeToWeb(code: String) {
        val q = JSONObject.quote(code)
        webView.post {
            webView.evaluateJavascript("""
              (function(){
                var code=$q;
                window.dispatchEvent(new CustomEvent('posnative:barcode',{detail:{code:code}}));
                if(window.VPE && typeof window.VPE.feedBarcode==='function') return;
                var inputs=[].slice.call(document.querySelectorAll('input')).filter(function(i){return i.offsetParent!==null;});
                var input=inputs.find(function(i){return /barcode/i.test([i.placeholder,i.getAttribute('aria-label'),i.name,i.id].filter(Boolean).join(' '));})
                      || inputs.find(function(i){return /search/i.test([i.placeholder,i.getAttribute('aria-label'),i.name,i.id].filter(Boolean).join(' '));});
                if(input){
                  input.focus();
                  var setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;
                  setter.call(input,code); input.dispatchEvent(new Event('input',{bubbles:true})); input.dispatchEvent(new Event('change',{bubbles:true}));
                  input.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,which:13,bubbles:true}));
                  input.dispatchEvent(new KeyboardEvent('keyup',{key:'Enter',code:'Enter',keyCode:13,which:13,bubbles:true}));
                }
              })();
            """.trimIndent(), null)
        }
    }

    inner class NativeBridge {
        @JavascriptInterface fun getAppVersion(): String = "0.2.0"
        @JavascriptInterface fun getPrinterStatus(): String = PrinterService(prefs).status()
        @JavascriptInterface fun getScannerName(): String = prefs.scannerLabel
        @JavascriptInterface fun openSettings() = runOnUiThread { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }

        @JavascriptInterface
        fun printReceipt(text: String) {
            Thread {
                val result = PrinterService(prefs).print(text)
                runOnUiThread { Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show() }
            }.start()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

private val Int.dp: Int get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
