package eu.siacs.conversations.ui.settings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.settings.userui.UserUIActivity;
import eu.siacs.conversations.utils.PhoneHelper;

public class NewSettingsAdapter extends RecyclerView.Adapter<NewSettingsViewHolder> {

    private ArrayList<String> itemList = new ArrayList<>();

    @NonNull
    @Override
    public NewSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_settings, parent, false);
        return new NewSettingsViewHolder(parent, view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewSettingsViewHolder holder, int position) {
        String itemStr = itemList.get(position);

        String uiOptions = holder.parent.getContext().getResources().getString(R.string.pref_ui_options);
        String aboutOptions = holder.parent.getContext().getResources().getString(R.string.title_activity_about);
        String aboutOptionsChild = holder.parent.getContext().getResources().getString(R.string.app_name) + PhoneHelper.getVersionName(holder.parent.getContext());
        String storageUIOptions = holder.parent.getContext().getResources().getString(R.string.new_setting_storage_ui);

        if (itemStr.equals(uiOptions)) {
            holder.tvSettingsContentFirst.setVisibility(View.VISIBLE);
            holder.tvSettingsContentFirst.setText(itemStr);
            holder.tvSettingContentParent.setVisibility(View.GONE);
            holder.tvSettingContentChild.setVisibility(View.GONE);
        } else if (itemStr.equals(aboutOptions)) {
            holder.tvSettingsContentFirst.setVisibility(View.GONE);
            holder.tvSettingContentParent.setVisibility(View.VISIBLE);
            holder.tvSettingContentChild.setVisibility(View.VISIBLE);
            holder.tvSettingContentParent.setText(itemStr);
            holder.tvSettingContentChild.setText(aboutOptionsChild);
        }else if (itemStr.equals(storageUIOptions)) {
            holder.tvSettingsContentFirst.setVisibility(View.VISIBLE);
            holder.tvSettingsContentFirst.setText(itemStr);
            holder.tvSettingContentParent.setVisibility(View.GONE);
            holder.tvSettingContentChild.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = null;
            if (position == 0) {
                intent = new Intent(holder.parent.getContext(), UserUIActivity.class);
            } else if(position == 1) {
                intent = new Intent(holder.parent.getContext(), AboutActivity.class);
            }else if (position == 2) {
                Toast.makeText(holder.parent.getContext(), "跳转存储", Toast.LENGTH_SHORT).show();
                intent = new Intent(holder.parent.getContext(), AboutActivity.class);
            }
            if(intent !=null) {
                holder.parent.getContext().startActivity(intent);
            }

        });

    }

    @Override
    public int getItemCount() {
        return itemList.isEmpty() ? 0 : itemList.size();
    }

    public void setItem(String item) {
        itemList.add(item);
        notifyDataSetChanged();
    }

}
