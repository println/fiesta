package proto.media.fiezta.shared.plugins

import android.content.Context
import android.net.Uri
import proto.media.fiezta.support.plugins.model.InvalidPluginException
import proto.media.fiezta.support.plugins.contract.PluginArchive
import proto.media.fiezta.support.plugins.archive.ZipPluginArchive
import java.io.File

object PluginZipInstaller {

    fun extract(context: Context, uri: Uri, archive: PluginArchive = ZipPluginArchive): File {
        val targetDir = File(context.cacheDir, "plugin-review-${System.nanoTime()}")
        val input = context.contentResolver.openInputStream(uri)
            ?: throw InvalidPluginException("could not open the selected file")
        return input.use { archive.unpack(it, targetDir) }
    }
}
