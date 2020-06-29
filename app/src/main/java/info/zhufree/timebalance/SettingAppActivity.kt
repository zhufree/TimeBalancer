package info.zhufree.timebalance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_setting_app.*

class SettingAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_app)
        setSupportActionBar(setting_toolbar)
        setting_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setting_toolbar?.setNavigationOnClickListener { finish() }
        val settingPagerAdapter = SettingPagerAdapter(this)
        vp_setting.adapter = settingPagerAdapter
        TabLayoutMediator(tab_setting, vp_setting, object : TabLayoutMediator.TabConfigurationStrategy {
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                // Styling each tab here
                tab.text = when (position) {
                    0 -> "增加时间"
                    1 -> "减少时间"
                    else -> ""
                }
            }
        }).attach()

    }
}