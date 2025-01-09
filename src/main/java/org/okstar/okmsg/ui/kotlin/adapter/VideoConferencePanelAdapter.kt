package org.okstar.okmsg.ui.kotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.siacs.conversations.R
import eu.siacs.conversations.databinding.ActivityVideoConferencePanelItemBinding
import org.okstar.okmsg.ui.kotlin.VideoConferenceActivity.Function2Meeting

class VideoConferencePanelAdapter:  RecyclerView.Adapter<VideoConferencePanelAdapter.ViewHolder>() {
    val mFunction2MeetingBeans: MutableList<Function2Meeting> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ActivityVideoConferencePanelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meetingBean = mFunction2MeetingBeans[position]
        holder.binding.activityVideoConferencePanelItemImage.apply {
                Glide.with(this.context)
                    .load(meetingBean.resId)
                    .placeholder(R.drawable.ic_workbench_pld)
                    .error(R.drawable.ic_workbench_pld)
                    .into(this)
        }
        holder.binding.activityVideoConferencePanelItemTitle.apply {
            text = meetingBean.nickName
        }
    }


    override fun getItemCount(): Int {
        return mFunction2MeetingBeans.size
    }

    fun setDatas(meetingBeans:MutableList<Function2Meeting>?){
        mFunction2MeetingBeans.clear()
        meetingBeans?.let { mFunction2MeetingBeans.addAll(it) }
        notifyDataSetChanged()
    }


    inner class ViewHolder(binding: ActivityVideoConferencePanelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding: ActivityVideoConferencePanelItemBinding = binding
    }
}