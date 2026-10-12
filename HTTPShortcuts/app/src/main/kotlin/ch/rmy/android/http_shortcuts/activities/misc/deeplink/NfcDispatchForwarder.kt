package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.net.Uri

/**
 * Decides what an NFC dispatch intent may forward to [DeepLinkActivity].
 * Only the NDEF action, the http-shortcuts scheme and the title/text extras are passed on.
 */
class NfcDispatchForwarder(
    private val forward: (Request) -> Unit,
) {
    data class Request(
        val data: Uri,
        val title: String?,
        val text: String?,
    )

    /** Cold start. A recreated instance (savedInstanceState != null) must not forward a second time. */
    fun onCreate(isRestored: Boolean, action: String?, data: Uri?, title: String?, text: String?) {
        if (!isRestored) {
            handle(action, data, title, text)
        }
    }

    /** Warm start: the activity instance already exists and receives a new tag. */
    fun onNewIntent(action: String?, data: Uri?, title: String?, text: String?) {
        handle(action, data, title, text)
    }

    private fun handle(action: String?, data: Uri?, title: String?, text: String?) {
        if (action != ACTION_NDEF_DISCOVERED || data == null || data.scheme != SCHEME) {
            return
        }
        forward(Request(data, title, text))
    }

    companion object {
        const val ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED"
        const val SCHEME = "http-shortcuts"
    }
}
