package proto.media.fiesta.features.domain.phone.browser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import proto.media.fiesta.R

class SiteStorageAdapter : RecyclerView.Adapter<SiteStorageAdapter.SiteViewHolder>() {

    class SiteEntry(val host: String, val cookieCount: Int, val hasWebStorage: Boolean)

    fun interface DeleteCallback {
        fun onDeleteSite(entry: SiteEntry)
    }

    private var entries: List<SiteEntry> = emptyList()
    private var deleteCallback: DeleteCallback? = null

    fun setDeleteCallback(deleteCallback: DeleteCallback?) {
        this.deleteCallback = deleteCallback
    }

    fun setEntries(entries: List<SiteEntry>) {
        this.entries = entries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_site_storage, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val entry = entries[position]
        holder.host.text = entry.host
        if (entry.cookieCount > 0) {
            holder.count.text = holder.count.resources
                .getString(R.string.storage_cookies_count, entry.cookieCount)
        } else {
            holder.count.setText(R.string.storage_no_data)
        }
        holder.delete.setOnClickListener {
            deleteCallback?.onDeleteSite(entry)
        }
    }

    override fun getItemCount(): Int = entries.size

    class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val host: TextView = itemView.findViewById(R.id.site_storage_host)
        val count: TextView = itemView.findViewById(R.id.site_storage_count)
        val delete: ImageButton = itemView.findViewById(R.id.site_storage_delete)
    }
}
