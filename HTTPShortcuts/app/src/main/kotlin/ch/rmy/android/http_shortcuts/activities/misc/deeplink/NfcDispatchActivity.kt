package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Receives NDEF_DISCOVERED for http-shortcuts:// tags. The manifest entry is protected by
 * android.permission.DISPATCH_NFC_MESSAGE, so only the system NFC service can start it.
 * It re-sends the link as a plain VIEW intent to [DeepLinkActivity] and finishes.
 */
class NfcDispatchActivity : Activity() {

    private val forwarder = NfcDispatchForwarder { request ->
        startActivity(
            Intent(Intent.ACTION_VIEW, request.data)
                .setClass(this, DeepLinkActivity::class.java)
                .putExtra(Intent.EXTRA_TITLE, request.title)
                .putExtra(Intent.EXTRA_TEXT, request.text),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forwarder.onCreate(savedInstanceState != null, intent?.action, intent?.data, intent.safeString(Intent.EXTRA_TITLE), intent.safeString(Intent.EXTRA_TEXT))
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        forwarder.onNewIntent(intent.action, intent.data, intent.safeString(Intent.EXTRA_TITLE), intent.safeString(Intent.EXTRA_TEXT))
        finish()
    }

    private fun Intent?.safeString(key: String): String? =
        try {
            this?.getStringExtra(key)
        } catch (_: RuntimeException) {
            null
        }
}
