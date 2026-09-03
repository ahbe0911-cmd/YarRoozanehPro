package ir.ahbe.instaclient

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var core: SingBoxManager
    private val directExecutor = Executor { it.run() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        web = findViewById(R.id.web)
        status = findViewById(R.id.status)
        progress = findViewById(R.id.progress)
        core = SingBoxManager(this)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString = userAgentString.replace("; wv", "")
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !isInstagram(request.url.host.orEmpty())
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }
        bindNavigation()
        val saved = getSharedPreferences("local", MODE_PRIVATE).getString("outbound", null)
        if (saved == null) requestConfig() else connect(saved)
    }

    private fun isInstagram(host: String) = host == "instagram.com" || host.endsWith(".instagram.com") ||
        host == "facebook.com" || host.endsWith(".facebook.com")

    private fun requestConfig() {
        val input = EditText(this).apply {
            hint = "کانفیگ JSON را اینجا Paste کنید"
            minLines = 8
            setPadding(36, 24, 36, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("راه‌اندازی اتصال امن")
            .setMessage("کانفیگ فقط در حافظه خصوصی گوشی نگهداری می‌شود و در هیچ گزارشی چاپ نخواهد شد.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("ذخیره و اتصال") { _, _ ->
                val value = input.text.toString().trim()
                getSharedPreferences("local", MODE_PRIVATE).edit().putString("outbound", value).apply()
                connect(value)
            }.show()
    }

    private fun connect(config: String) {
        status.text = "در حال اتصال…"
        Thread {
            val ok = runCatching { core.start(config) }.getOrDefault(false)
            runOnUiThread {
                if (!ok) {
                    status.text = "اتصال ناموفق"
                    requestConfig()
                    return@runOnUiThread
                }
                if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                    status.text = "WebView ناسازگار"
                    return@runOnUiThread
                }
                val proxy = ProxyConfig.Builder().addProxyRule(core.proxyUrl()).build()
                ProxyController.getInstance().setProxyOverride(proxy, directExecutor) {
                    runOnUiThread {
                        status.text = "● متصل"
                        web.loadUrl("https://www.instagram.com/")
                    }
                }
            }
        }.start()
    }

    private fun bindNavigation() {
        val links = mapOf(
            R.id.home to "https://www.instagram.com/",
            R.id.explore to "https://www.instagram.com/explore/",
            R.id.reels to "https://www.instagram.com/reels/",
            R.id.direct to "https://www.instagram.com/direct/inbox/",
            R.id.profile to "https://www.instagram.com/accounts/edit/"
        )
        links.forEach { (id, url) -> findViewById<Button>(id).setOnClickListener { web.loadUrl(url) } }
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (isFinishing) core.stop()
        super.onDestroy()
    }
}
