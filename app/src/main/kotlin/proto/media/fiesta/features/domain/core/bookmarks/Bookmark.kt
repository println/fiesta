package proto.media.fiesta.features.domain.core.bookmarks
import androidx.annotation.IdRes
import io.realm.RealmObject

open class Bookmark() : RealmObject() {

    var title: String? = null
    var url: String? = null
    var thumbnail: ByteArray? = null
    var createdAt: Long = 0

    @IdRes
    var thumbnailResource: Int = 0

    var isPreventDelete: Boolean = false
    var sortOrder: Int = 0

    constructor(title: String?, url: String?, thumbnailRes: Int) : this() {
        this.title = title
        this.url = url
        this.thumbnailResource = thumbnailRes
        this.isPreventDelete = true
        this.createdAt = System.currentTimeMillis()
    }
}
