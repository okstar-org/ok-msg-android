package org.okstar.okmsg.ui.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import eu.siacs.conversations.R
import eu.siacs.conversations.databinding.ActivityVideoConferenceBinding
import eu.siacs.conversations.ui.XmppActivity
import org.okstar.okmsg.ui.kotlin.adapter.VideoConferencePanelAdapter

/**
 * 视频会议
 */
class VideoConferenceActivity : XmppActivity() {
    private var binding: ActivityVideoConferenceBinding? = null

    private val mAdapter: VideoConferencePanelAdapter by lazy { VideoConferencePanelAdapter() }

    private val functionBeans = mutableListOf(Function2Meeting.SendMeeting,Function2Meeting.AddMeeting,Function2Meeting.MakeMeeting,Function2Meeting.NoteMeeting,Function2Meeting.PhoneMeeting)

    override fun refreshUiReal() {
    }

    override fun onBackendConnected() {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_conference)
        setSupportActionBar(binding?.toolbar as Toolbar)
        configureActionBar(supportActionBar)

        initRecyclerView()

        //doWorkbenchRequest()
    }

    private fun initRecyclerView(){
        mAdapter.setDatas(functionBeans)
        binding?.videoConferencePanelView?.apply {
            layoutManager = GridLayoutManager(context,4)
            adapter = mAdapter
        }
    }


    /*
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
                return@doWorkbench
            }
            mAdapter.setDatas(workbenchBeans)
            if(workbenchBeans.isNullOrEmpty()){
                binding?.workbenchPanelEmpty?.apply {
                    visibility = View.VISIBLE
                    text = "工作台为空"
                }
            }
            Log.w(Config.LOGTAG, "工作平台获取成功...")
        }

    }
     */

    enum class Function2Meeting(val index: Int,val nickName: String, val resId:Int){
        SendMeeting(index = 1, nickName = "发起会议", resId = R.drawable.video_conference_send),
        AddMeeting(index = 2, nickName = "加入会议", resId = R.drawable.video_conference_add),
        MakeMeeting(index = 3, nickName = "预约会议", resId = R.drawable.video_conference_make),
        NoteMeeting(index = 4, nickName = "妙记", resId = R.drawable.video_conference_note),
        PhoneMeeting(index = 5, nickName = "电话", resId = R.drawable.video_conference_phone)
    }

    companion object {
        fun launch(activity: Context) {
            val intent = Intent(activity, VideoConferenceActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

