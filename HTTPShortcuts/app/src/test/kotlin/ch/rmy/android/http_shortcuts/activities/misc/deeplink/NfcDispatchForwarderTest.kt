package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NfcDispatchForwarderTest {

    private val forwarded = mutableListOf<NfcDispatchForwarder.Request>()
    private val forwarder = NfcDispatchForwarder { forwarded += it }

    private fun uri(scheme: String?) = mockk<Uri> { every { this@mockk.scheme } returns scheme }

    private val ndef = NfcDispatchForwarder.ACTION_NDEF_DISCOVERED

    @Test
    fun `cold start forwards data and extras`() {
        val data = uri("http-shortcuts")
        forwarder.onCreate(false, ndef, data, "title", "text")
        assertEquals(listOf(NfcDispatchForwarder.Request(data, "title", "text")), forwarded)
    }

    @Test
    fun `recreated instance does not forward again`() {
        forwarder.onCreate(true, ndef, uri("http-shortcuts"), null, null)
        assertTrue(forwarded.isEmpty())
    }

    @Test
    fun `warm start forwards each new tag`() {
        val first = uri("http-shortcuts")
        val second = uri("http-shortcuts")
        forwarder.onCreate(false, ndef, first, null, null)
        forwarder.onNewIntent(ndef, second, null, null)
        assertEquals(listOf(first, second), forwarded.map { it.data })
    }

    @Test
    fun `foreign action is dropped`() {
        forwarder.onCreate(false, "android.intent.action.VIEW", uri("http-shortcuts"), null, null)
        forwarder.onNewIntent("android.intent.action.SEND", uri("http-shortcuts"), null, null)
        assertTrue(forwarded.isEmpty())
    }

    @Test
    fun `foreign scheme or missing data is dropped`() {
        forwarder.onCreate(false, ndef, uri("https"), null, null)
        forwarder.onNewIntent(ndef, uri(null), null, null)
        forwarder.onNewIntent(ndef, null, null, null)
        assertTrue(forwarded.isEmpty())
    }
}
