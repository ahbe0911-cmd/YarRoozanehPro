package ir.ahbe.instaclient

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

class SingBoxManager(private val context: Context) {
    private var process: Process? = null
    private val port = 20808

    fun start(outboundJson: String): Boolean {
        stop()
        val supplied = JSONObject(outboundJson)
        val outbound = supplied.optJSONArray("outbounds")?.optJSONObject(0)
            ?: supplied.optJSONObject("outbound")
            ?: if (supplied.optString("type").isNotBlank()) supplied else null
            ?: error("کانفیگ VMess معتبر نیست")
        require(outbound.optString("type") == "vmess") { "فقط کانفیگ VMess پذیرفته می‌شود" }
        if (outbound.optString("tag").isBlank()) outbound.put("tag", "proxy")

        val config = JSONObject().apply {
            put("log", JSONObject().put("level", "warn").put("output", File(context.cacheDir, "core.log").path))
            put("inbounds", JSONArray().put(JSONObject()
                .put("type", "mixed").put("tag", "local")
                .put("listen", "127.0.0.1").put("listen_port", port)))
            put("outbounds", JSONArray().put(outbound).put(JSONObject().put("type", "direct").put("tag", "direct")))
            put("route", JSONObject().put("final", outbound.optString("tag")))
        }
        val configFile = File(context.filesDir, "runtime.json").apply { writeText(config.toString()) }
        val binary = File(context.applicationInfo.nativeLibraryDir, "libsingbox.so")
        require(binary.canExecute()) { "هسته اتصال داخل برنامه پیدا نشد" }
        process = ProcessBuilder(binary.path, "run", "-c", configFile.path)
            .redirectErrorStream(true).start()

        repeat(50) {
            if (process?.isAlive != true) return false
            runCatching {
                Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 150) }
            }.onSuccess { return true }
            Thread.sleep(100)
        }
        stop()
        return false
    }

    fun stop() {
        process?.destroy()
        process = null
    }

    fun proxyUrl() = "socks5://127.0.0.1:$port"
}
