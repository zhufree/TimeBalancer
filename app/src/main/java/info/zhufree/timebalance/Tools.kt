package info.zhufree.timebalance

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo


object Tools {
    fun dp2px(dpValue: Int, context: Context = TimeApplication.context): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


}