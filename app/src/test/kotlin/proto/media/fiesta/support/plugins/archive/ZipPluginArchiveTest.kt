package proto.media.fiesta.support.plugins.archive

import proto.media.fiesta.support.plugins.model.InvalidPluginException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipPluginArchiveTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun zipOf(vararg entries: Pair<String, String>): ByteArrayInputStream {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            for ((name, content) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return ByteArrayInputStream(bytes.toByteArray())
    }

    private fun target() = File(temp.root, "review")

    @Test
    fun unpacksAPluginWithTheManifestAtTheRoot() {
        val root = ZipPluginArchive.unpack(zipOf("manifest.json" to "{}", "a.tweak.js" to "run();"), target())

        assertEquals(target(), root)
        assertEquals("run();", File(root, "a.tweak.js").readText())
    }

    @Test
    fun findsThePluginInsideASingleWrappingFolder() {
        val root = ZipPluginArchive.unpack(zipOf("myplugin/manifest.json" to "{}"), target())

        assertEquals(File(target(), "myplugin"), root)
    }

    @Test(expected = InvalidPluginException::class)
    fun rejectsEntriesThatEscapeTheTargetFolder() {
        ZipPluginArchive.unpack(zipOf("../evil.js" to "x"), target())
    }

    @Test(expected = InvalidPluginException::class)
    fun rejectsAnArchiveWithoutManifest() {
        ZipPluginArchive.unpack(zipOf("a.tweak.js" to "run();"), target())
    }
}
