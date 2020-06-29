package info.zhufree.timebalance

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import info.zhufree.timebalance.PreferenceUtil.ADD_TIME_TYPE
import info.zhufree.timebalance.PreferenceUtil.REMOVE_TIME_TYPE
import kotlinx.android.synthetic.main.fragment_app_setting.*


private const val ARG_POSITION = "position"

class AppSettingFragment : Fragment() {
    private var position: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            val packages = it.packageManager.getInstalledPackages(0).filter { pkgInfo ->
                (pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            val appAdapter = if (position == 0) {
                AppAdapter(it, packages, it.packageManager, ADD_TIME_TYPE)
            } else {
                AppAdapter(it, packages, it.packageManager, REMOVE_TIME_TYPE)
            }
            rv_app_list.adapter = appAdapter

            val staggerLayoutManager = StaggeredGridLayoutManager(
                4, StaggeredGridLayoutManager.VERTICAL
            )
            rv_app_list.layoutManager = staggerLayoutManager
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(position: Int) =
            AppSettingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
    }
}