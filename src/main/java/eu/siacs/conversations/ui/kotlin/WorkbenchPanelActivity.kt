package eu.siacs.conversations.ui.kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import eu.siacs.conversations.Config
import eu.siacs.conversations.R
import eu.siacs.conversations.databinding.ActivityWorkbenchPanelBinding
import eu.siacs.conversations.ui.XmppActivity
import eu.siacs.conversations.ui.kotlin.adapter.WorkbenchPanelAdapter
import eu.siacs.conversations.ui.util.ConstCommon
import org.okstar.okmsg.volley.bean.WorkbenchBean
import org.okstar.okmsg.volley.request.doWorkbench

/**
 * 工作台
 */
class WorkbenchPanelActivity : XmppActivity() {
    private var binding: ActivityWorkbenchPanelBinding? = null

    private val mAdapter: WorkbenchPanelAdapter by lazy {  WorkbenchPanelAdapter() }


    override fun refreshUiReal() {
    }

    override fun onBackendConnected() {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_workbench_panel)
        setSupportActionBar(binding?.toolbar as Toolbar)
        configureActionBar(supportActionBar)

        binding?.workbenchPanelLoadingProgressbar?.visibility = View.VISIBLE

        initRecyclerView()

        doWorkbenchRequest()
    }

    private fun initRecyclerView(){
        binding?.workbenchPanelView?.apply {
            layoutManager = GridLayoutManager(context,3)
            adapter = mAdapter
        }
    }

    private fun doWorkbenchRequest() {
        val hostName = mmkv.getString(ConstCommon.stackUrl,"")?:""
        val accessToken = mmkv.getString(ConstCommon.accessToken,"")?:""

        doWorkbench(
            hostName,
            token = accessToken,
            pageIndex = 0,
            pageSize = 100
        ) { workbenchBeans: MutableList<WorkbenchBean>?, integer: Int, s: String ->
            binding?.workbenchPanelLoadingProgressbar?.visibility = View.GONE

            if (integer != 0) {
                Log.w(
                    Config.LOGTAG,
                    "工作平台获取失败，错误信息：$s"
                )
                binding?.workbenchPanelEmpty?.apply {
                    visibility = View.VISIBLE
                    text = "出错啦~"
                }
//                binding.loadingProgressbar.setVisibility(View.GONE)
                return@doWorkbench
            }
            mAdapter.setDatas(workbenchBeans)
            if(workbenchBeans.isNullOrEmpty()){
                binding?.workbenchPanelEmpty?.apply {
                    visibility = View.VISIBLE
                    text = "工作台为空"
                }
            }else{
                binding?.workbenchPanelEmpty?.visibility = View.GONE
            }
            Log.w(Config.LOGTAG, "工作平台获取成功...")
        }

    }


    companion object {
        fun launch(activity: Context) {
            val intent = Intent(activity, WorkbenchPanelActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

