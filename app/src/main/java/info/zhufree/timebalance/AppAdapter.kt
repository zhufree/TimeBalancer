package info.zhufree.timebalance

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_app.view.*
import org.jetbrains.anko.image

class AppAdapter(
    val context: Context,
    val packages: List<PackageInfo>,
    val packageManager: PackageManager,
    val appType: Int
) : RecyclerView.Adapter<AppAdapter.AppHolder>() {
    class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
        var isChecked = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(LayoutInflater.from(context).inflate(R.layout.item_app, parent, false))
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        val pkgInfo = packages[position]
        holder.itemView.iv_app_icon.image = pkgInfo.applicationInfo.loadIcon(packageManager)
        holder.itemView.tv_app_name.text =
            pkgInfo.applicationInfo.loadLabel(packageManager).toString()
        val pkgNameSet = LinkedHashSet<String>()
        PreferenceUtil.getAppByType(appType).forEach { pkgNameSet.add(it) }

        Log.i("apps", pkgNameSet.toString())
        holder.itemView.v_app_check_border.visibility = if (pkgInfo.packageName in pkgNameSet)
            VISIBLE else GONE
        holder.itemView.setOnClickListener {
            if (holder.isChecked) {
                holder.itemView.v_app_check_border.visibility = GONE
                pkgNameSet.remove(pkgInfo.packageName)
                holder.isChecked = false
                PreferenceUtil.setAppByType(appType, pkgNameSet)
            } else {
                holder.itemView.v_app_check_border.visibility = VISIBLE
                pkgNameSet.add(pkgInfo.packageName)
                holder.isChecked = true
                PreferenceUtil.setAppByType(appType, pkgNameSet)
            }
        }
    }
}