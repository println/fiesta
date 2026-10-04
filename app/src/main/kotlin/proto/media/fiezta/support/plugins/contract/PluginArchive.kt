package proto.media.fiezta.support.plugins.contract

import java.io.File
import java.io.InputStream

interface PluginArchive {

    fun unpack(input: InputStream, targetDir: File): File
}
