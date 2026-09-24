package proto.media.fiesta.support.plugins.archive

import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.contract.PluginArchive
import proto.media.fiesta.support.plugins.model.InvalidPluginException
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

object ZipPluginArchive : PluginArchive {

    override fun unpack(input: InputStream, targetDir: File): File {
        targetDir.mkdirs()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.contains("..")) {
                    throw InvalidPluginException("invalid entry in zip: ${entry.name}")
                }
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { output -> zip.copyTo(output) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return pluginRoot(targetDir)
    }

    private fun pluginRoot(dir: File): File {
        if (File(dir, PluginManifest.FILE_NAME).exists()) return dir
        val subdirs = dir.listFiles { file -> file.isDirectory } ?: emptyArray()
        val nested = subdirs.singleOrNull { File(it, PluginManifest.FILE_NAME).exists() }
        return nested ?: throw InvalidPluginException("the zip does not contain a manifest.json")
    }
}
