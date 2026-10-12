package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.w3c.dom.Element

class NfcDispatchManifestTest {

    private val android = "http://schemas.android.com/apk/res/android"

    private val activities: List<Element> by lazy {
        val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(File("src/main/AndroidManifest.xml"))
        val nodes = doc.getElementsByTagName("activity")
        (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun activity(name: String) =
        assertNotNull(activities.firstOrNull { it.getAttributeNS(android, "name") == name }, "missing $name")

    private fun Element.actions(): List<String> =
        getElementsByTagName("action").let { n -> (0 until n.length).map { (n.item(it) as Element).getAttributeNS(android, "name") } }

    @Test
    fun `dispatch activity is permission protected and receives NDEF`() {
        val a = activity(".activities.misc.deeplink.NfcDispatchActivity")
        assertEquals("android.permission.DISPATCH_NFC_MESSAGE", a.getAttributeNS(android, "permission"))
        assertEquals("true", a.getAttributeNS(android, "exported"))
        assertEquals(listOf("android.nfc.action.NDEF_DISCOVERED"), a.actions())
        val schemes = a.getElementsByTagName("data").let { n -> (0 until n.length).map { (n.item(it) as Element).getAttributeNS(android, "scheme") } }
        assertEquals(listOf("http-shortcuts"), schemes)
    }

    @Test
    fun `deep link activity keeps VIEW filters but no longer takes NDEF directly`() {
        val a = activity(".activities.misc.deeplink.DeepLinkActivity")
        assertEquals(listOf("android.intent.action.VIEW", "android.intent.action.VIEW"), a.actions())
        assertTrue("android.nfc.action.NDEF_DISCOVERED" !in a.actions())
    }

    @Test
    fun `launcher activity is preserved`() {
        val launcher = activities.filter { it.actions().contains("android.intent.action.MAIN") }
        assertTrue(launcher.any { it.getAttributeNS(android, "name") == ".activities.main.MainActivity" })
    }
}
