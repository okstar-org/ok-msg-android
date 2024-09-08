package eu.siacs.conversations.ui.kotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import eu.siacs.conversations.R
import eu.siacs.conversations.databinding.ActivityWorkbenchPanelItemBinding
import org.okstar.okmsg.volley.bean.WorkbenchBean

class WorkbenchPanelAdapter:  RecyclerView.Adapter<WorkbenchPanelAdapter.ViewHolder>() {
    val mWorkbenchBeans: MutableList<WorkbenchBean> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ActivityWorkbenchPanelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workbenchBean = mWorkbenchBeans[position]
        holder.binding.workbenchPanelImage.apply {
                Glide.with(this.context)
                    .load(workbenchBean.avatar)
                    .placeholder(R.drawable.ic_workbench_pld)
                    .error(R.drawable.ic_workbench_pld)
                    .into(this)
        }
        holder.binding.workbenchPanelTitle.apply {
            text = workbenchBean.name
        }
    }


    override fun getItemCount(): Int {
        return mWorkbenchBeans.size
    }

    fun setDatas(workbenchBeans:MutableList<WorkbenchBean>?){
        mWorkbenchBeans.clear()
        workbenchBeans?.let { mWorkbenchBeans.addAll(it) }
        notifyDataSetChanged()
    }


    inner class ViewHolder(binding: ActivityWorkbenchPanelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding: ActivityWorkbenchPanelItemBinding = binding
    }
}