
package com.geosul.geotools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Vosk
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private val voskReady = AtomicBoolean(false)

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val bmp = res.data?.extras?.get("data") as? Bitmap
            if (bmp != null) runOcr(bmp) else sendJs("error", "Falha ao capturar imagem")
        } else sendJs("error", "Captura cancelada")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), 123
        )

        setupWebView()

        // Carrega modelo Vosk de assets -> storage interno (não bloquear a UI)
        StorageService.unpack(this, "models/pt", "model-pt",
            { modelPath ->
                try {
                    model = Model(modelPath)
                    recognizer = Recognizer(model, 16000.0f)
                    voskReady.set(true)
                    sendJs("stt", "modelo offline pronto")
                } catch (e: Exception) {
                    sendJs("error", "Vosk erro: " + e.message)
                }
            },
            { ex -> sendJs("error", "Falha ao preparar modelo: " + ex.message) })
    }

    private fun setupWebView() {
        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.allowFileAccess = true
        s.allowFileAccessFromFileURLs = true
        s.allowUniversalAccessFromFileURLs = true

        webView.webViewClient = object : WebViewClient() {}
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidBridge(), "Android")
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    // ---------- OCR ----------
    private fun runOcr(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizerMl = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizerMl.process(image)
            .addOnSuccessListener { result -> sendJs("ocr", result.text ?: "") }
            .addOnFailureListener { e -> sendJs("error", "Erro OCR: ${e.message}") }
    }

    // ---------- Vosk STT ----------
    private fun startSttVosk() {
        if (!voskReady.get()) { sendJs("error", "Modelo Vosk ainda carregando"); return }
        try {
            stopSttVosk()
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening { result ->
                // result é JSON com text/partial
                // Extraímos "text" simples para enviar ao JS
                val txt = result?.result ?: result?.text ?: ""
                if (!txt.isNullOrBlank()) sendJs("stt", txt)
            }
            sendJs("stt", "escutando (offline)")
        } catch (e: Exception) {
            sendJs("error", "Vosk start erro: " + e.message)
        }
    }

    private fun stopSttVosk() {
        try {
            speechService?.stop()
            speechService?.shutdown()
            speechService = null
        } catch (_: Exception) {}
    }

    // ---------- Ponte JS ----------
    inner class AndroidBridge {
        @JavascriptInterface fun scanText() {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
        @JavascriptInterface fun startListening() = startSttVosk()
        @JavascriptInterface fun stopListening() = stopSttVosk()
        @JavascriptInterface fun speak(text: String) {
            // Mantivemos sem TTS para simplificar aqui; navegadores farão TTS via JS se necessário.
        }
    }

    private fun sendJs(type: String, payload: String) {
        runOnUiThread {
            val safe = payload.replace("\\", "\\\\").replace("'", "\\'").replace("\\n", "\\\\n").replace("\n", "\\n")
            webView.evaluateJavascript("window.onNativeResult('$type', '$safe')", null)
        }
    }

    override fun onDestroy() {
        stopSttVosk()
        super.onDestroy()
    }
}
