package proto.media.fiesta.features.domain.mediaserver

import android.support.v4.media.MediaBrowserCompat

interface BrowseTree {
    fun rootFor(clientPackageName: String): String
    fun childrenOf(rootId: String): List<MediaBrowserCompat.MediaItem>
    fun onRootBrowsed(rootId: String)
}
