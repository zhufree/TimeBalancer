package info.zhufree.timebalance

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceUtil {
    const val TIME_SP = "TIME_SP"
    const val APP_SP = "APP_SP"
    const val ADD_TIME_TYPE = 0
    const val REMOVE_TIME_TYPE = 1
    const val INTERVAL_TYPE_DAY = 0
    const val INTERVAL_TYPE_WEEK = 1

    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return TimeApplication.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun setAppByType(type: Int, appPkgName: Set<String>) {
        getPrivateSharedPreference(APP_SP)?.edit {
            putStringSet(
                if (type == ADD_TIME_TYPE) "add_time_apps" else "remove_time_apps",
                appPkgName
            )
        }
    }

    fun getAppByType(type: Int): MutableSet<String> {
        return getPrivateSharedPreference(APP_SP)?.getStringSet(
            if (type == ADD_TIME_TYPE) "add_time_apps" else "remove_time_apps",
            LinkedHashSet<String>()
        ) ?: LinkedHashSet()
    }

    fun setIntervalType(type: Int) {
        setIntValue(APP_SP, "interval", type)
    }

    fun getIntervalType(): Int {
        return getIntValue(APP_SP, "interval")
    }

    /**
     * 工具方法
     */
    private fun getBooleanValue(
        spName: String,
        key: String,
        defaultValue: Boolean = false
    ): Boolean {
        return getPrivateSharedPreference(spName)?.getBoolean(key, defaultValue) ?: defaultValue
    }

    private fun setBooleanValue(spName: String, key: String, value: Boolean) {
        getPrivateSharedPreference(spName)?.edit {
            putBoolean(key, value)
        }
    }

    fun getIntValue(spName: String, key: String): Int {
        return getPrivateSharedPreference(spName)?.getInt(key, 0) ?: 0
    }

    fun setIntValue(spName: String, key: String, value: Int) {
        getPrivateSharedPreference(spName)?.edit {
            putInt(key, value)
        }
    }

    private fun getLongValue(spName: String, key: String): Long {
        return getPrivateSharedPreference(spName)?.getLong(key, 0L) ?: 0L
    }

    private fun setLongValue(spName: String, key: String, value: Long) {
        getPrivateSharedPreference(spName)?.edit {
            putLong(key, value)
        }
    }

    private fun getStringValue(spName: String, key: String): String {
        return getPrivateSharedPreference(spName)?.getString(key, "") ?: ""
    }

    private fun setStringValue(spName: String, key: String, value: String) {
        getPrivateSharedPreference(spName)?.edit {
            putString(key, value)
        }
    }
}