package com.digitalchopda.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var pendingVerificationId: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.webChromeClient = WebChromeClient()
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(ChopdaBridge(), "AndroidChopda")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class ChopdaBridge {
        @JavascriptInterface fun getUserUid(): String = auth.currentUser?.uid ?: ""
        @JavascriptInterface fun getUserEmail(): String = auth.currentUser?.email ?: ""
        @JavascriptInterface fun isLoggedIn(): Boolean = auth.currentUser != null

        @JavascriptInterface fun openWhatsApp(phone: String, message: String) {
            runOnUiThread {
                val digits = phone.filter { it.isDigit() }
                val uri = Uri.parse("https://wa.me/$digits?text=" + Uri.encode(message))
                try { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                catch (_: Exception) { Toast.makeText(this@MainActivity, "WhatsApp link open nahi hua", Toast.LENGTH_SHORT).show() }
            }
        }

        // Firebase phone OTP and email sign-in are exposed for the login UI integration.
        @JavascriptInterface fun sendPhoneOtp(e164Phone: String) {
            runOnUiThread {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(e164Phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this@MainActivity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            auth.signInWithCredential(credential).addOnFailureListener {
                                Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                            Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                            pendingVerificationId = id
                            Toast.makeText(this@MainActivity, "OTP bhej diya gaya. Abhi OTP verification UI connect karna baaki hai.", Toast.LENGTH_LONG).show()
                        }
                    }).build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
