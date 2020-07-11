package info.zhufree.timebalance

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.InputType.TYPE_CLASS_NUMBER
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import info.zhufree.timebalance.PreferenceUtil.ADD_TIME_TYPE
import info.zhufree.timebalance.PreferenceUtil.INTERVAL_TYPE_DAY
import info.zhufree.timebalance.PreferenceUtil.INTERVAL_TYPE_WEEK
import info.zhufree.timebalance.PreferenceUtil.REMOVE_TIME_TYPE
import info.zhufree.timebalance.PreferenceUtil.TIME_SP
import info.zhufree.timebalance.Tools.dp2px
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity() {
    var remainTime = PreferenceUtil.getIntValue(TIME_SP, "remain_time") // 总剩余时间
    var manualTime = PreferenceUtil.getIntValue(TIME_SP, "manual_time") // 手动输入的时间计算后的剩余
    var autoTime = PreferenceUtil.getIntValue(TIME_SP, "auto_time") // 按app使用自动计算的时间剩余
    // remainTime = autoTime + manualTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )

        val granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
        if (!granted) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            getDailyStats()
        }

        // show remain time
        showRemainTime()

        btn_add_time.setOnClickListener {
            showInputDialog(0)
        }
        btn_remove_time.setOnClickListener {
            showInputDialog(1)
        }
        btn_reset.setOnClickListener {
            PreferenceUtil.setIntValue(TIME_SP, "manual_time", 0)
            getDailyStats()
        }

        btn_interval_day.setOnClickListener {
            PreferenceUtil.setIntervalType(INTERVAL_TYPE_DAY)
            getDailyStats()
        }
        btn_interval_week.setOnClickListener {
            PreferenceUtil.setIntervalType(INTERVAL_TYPE_WEEK)
            getDailyStats()
        }

        btn_set_app.setOnClickListener { startActivity<SettingAppActivity>() }
    }

    val ADD_TIME = 0
    val REMOVE_TIME = 1

    fun showInputDialog(type: Int) {
        var input: EditText? = null
        alert {
            customView {
                linearLayout {
                    padding = dip(8)
                    orientation = LinearLayout.VERTICAL
                    textView("输入时间（min）") {
                        textColor = R.color.colorPrimaryDark
                        textSize = 18f
                    }
                    input = editText {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        singleLine = true
                        inputType = TYPE_CLASS_NUMBER
                    }
                }

            }
            positiveButton("确定") {
                input?.clearFocus()
                val deltaTime = (input?.text?.toString()?.toInt() ?: 0)
                val time =
                    if (type == ADD_TIME) (manualTime + deltaTime) else (manualTime - deltaTime)
                PreferenceUtil.setIntValue(TIME_SP, "manual_time", time)
                PreferenceUtil.setIntValue(TIME_SP, "remain_time", autoTime + time)
                showRemainTime()
            }
            negativeButton("取消") {}
        }.show()
    }

    private fun showRemainTime() {
        remainTime = PreferenceUtil.getIntValue(TIME_SP, "remain_time")
        manualTime = PreferenceUtil.getIntValue(TIME_SP, "manual_time")
        autoTime = PreferenceUtil.getIntValue(TIME_SP, "auto_time")

        if (remainTime == 0) {
            tv_battery_number.visibility = GONE
        } else {
            tv_battery_number.visibility = VISIBLE
        }
        tv_battery_number.text = remainTime.toString()
        val layoutLp = v_battery.layoutParams as ConstraintLayout.LayoutParams
        val height = when (remainTime) {
            in 0..50 -> { // 0-60
                remainTime * 4
            }
            in 50..110 -> { // 60-120
                remainTime * 2
            }
            in 110..340 -> { // 120-360
                (remainTime * 0.8f).toInt()
            }
            in 340..550 -> { // 360-600
                (remainTime * 0.4f).toInt()
            }
            else -> 0
        }
        layoutLp.height = dp2px(height)
    }

    private fun getDailyStats(): List<Stat> {
        val addTimeApp = PreferenceUtil.getAppByType(ADD_TIME_TYPE)
        val removeTimeApp = PreferenceUtil.getAppByType(REMOVE_TIME_TYPE)
        val allApp = addTimeApp + removeTimeApp
        val intervalType = PreferenceUtil.getIntervalType()
        val calendar = getInstance()
        calendar.run {
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }
        val todayStartTime = calendar.timeInMillis
        calendar.add(DAY_OF_WEEK, 1)
        val todayEndTime = calendar.timeInMillis
        // 设置一周的起始时间和结束时间
        calendar.add(DAY_OF_WEEK, -1)
        if (calendar.get(DAY_OF_WEEK) == 1) {
            // sunday
            calendar.add(WEEK_OF_MONTH, -1) // 退回周六
            calendar.set(DAY_OF_WEEK, 2)// 设置周一
        } else {
            calendar.set(DAY_OF_WEEK, 2)
        }
        val weekStartTime = calendar.timeInMillis
        calendar.add(DAY_OF_WEEK, 7)
        val weekEndTime = calendar.timeInMillis

        val sortedEvents = mutableMapOf<String, MutableList<UsageEvents.Event>>()
        val mUsageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val systemEvents =
            mUsageStatsManager.queryEvents(if (intervalType == INTERVAL_TYPE_DAY) todayStartTime
            else weekStartTime, System.currentTimeMillis()) // get events
        while (systemEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            systemEvents.getNextEvent(event)
            val packageEvents = sortedEvents[event.packageName] ?: mutableListOf()
            packageEvents.add(event)
            sortedEvents[event.packageName] = packageEvents
        }

        val stats = mutableListOf<Stat>()

        val packageNames = sortedEvents.keys
        for (pkgName in packageNames) {
            if (pkgName in allApp) {
                var eventStartTime = 0L
                var eventEndTime = 0L
                var totalTime = 0L
                val pkgEvents = sortedEvents[pkgName]
                pkgEvents?.forEach {
                    if (it.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        eventStartTime = it.timeStamp
                    } else if (it.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        eventEndTime = it.timeStamp
                    }

                    if (eventStartTime == 0L && eventEndTime != 0L) {
                        if (pkgEvents.indexOf(it) < 2) {
                            eventStartTime = if (intervalType == INTERVAL_TYPE_DAY) todayStartTime else weekStartTime
                        } else {
                            eventEndTime = 0L
                        }
                    }

                    if (eventStartTime != 0L && eventEndTime != 0L) {
                        totalTime += eventEndTime - eventStartTime
                        eventStartTime = 0L
                        eventEndTime = 0L
                    }

                }

                if (eventStartTime != 0L && eventEndTime == 0L) {
                    totalTime += (if (intervalType == INTERVAL_TYPE_DAY) todayEndTime else weekEndTime) - 1000 - eventStartTime
                    eventStartTime = 0L
                }

                stats.add(Stat(pkgName, (totalTime / 60000).toInt()))
            }
        }

        var posAppUseLog = ""
        var negAppUseLog = ""
        var autoTime = 0
        stats.forEach {
            if (it.packageName in addTimeApp) {
                posAppUseLog += "${getAppName(it.packageName)} : ${it.totalTime}min\n"
                autoTime += it.totalTime
            } else {
                negAppUseLog += "${getAppName(it.packageName)} : ${it.totalTime}min\n"
                autoTime -= it.totalTime
            }
        }
        PreferenceUtil.setIntValue(TIME_SP, "auto_time", autoTime)
        PreferenceUtil.setIntValue(TIME_SP, "remain_time", autoTime + manualTime)
        showRemainTime()
        tv_pos_app_use_log.text = posAppUseLog
        tv_neg_app_use_log.text = negAppUseLog
        return stats
    }

    class Stat(val packageName: String, val totalTime: Int)

    override fun onResume() {
        super.onResume()
        getDailyStats()
    }

    fun getAppName(packageName: String ): String {
        val packages = packageManager.getInstalledPackages(0)
        packages.forEach {
            if (it.applicationInfo.packageName == packageName) { //非系统应用
                return it.applicationInfo.loadLabel(packageManager).toString()
            }
        }
        return ""
    }
}