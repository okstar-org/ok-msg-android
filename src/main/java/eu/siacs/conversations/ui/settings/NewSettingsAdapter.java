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
import eu.siacs.conversations.ui.AboutActivity;
import eu.siacs.conversations.ui.settings.userui.UserUIActivity;
import eu.siacs.conversations.utils.PhoneHelper;

public class NewSettingsAdapter extends RecyclerView.Adapter<NewSettingsViewHolder> {

    private ArrayList<String> itemList = new ArrayList<>();

    @NonNull
    @Override
    public NewSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_settings, parent, false);
        return new NewSettingsViewHolder(parent,view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewSettingsViewHolder holder, int position) {
        String itemStr = itemList.get(position);

        String uiOptions = holder.parent.getContext().getResources().getString(R.string.pref_ui_options);
        String aboutOptions = holder.parent.getContext().getResources().getString(R.string.title_activity_about);
        String aboutOptionsChild = holder.parent.getContext().getResources().getString(R.string.app_name) + PhoneHelper.getVersionName(holder.parent.getContext());
        if(itemStr.equals(uiOptions)) {
            holder.tvSettingsContentFirst.setVisibility(View.VISIBLE);
            holder.tvSettingContentParent.setVisibility(View.GONE);
            holder.tvSettingContentChild.setVisibility(View.GONE);
            holder.tvSettingsContentFirst.setText(itemStr);
        }else if (itemStr.equals(aboutOptions)){
            holder.tvSettingsContentFirst.setVisibility(View.GONE);
            holder.tvSettingContentParent.setVisibility(View.VISIBLE);
            holder.tvSettingContentChild.setVisibility(View.VISIBLE);
            holder.tvSettingContentParent.setText(itemStr);
            holder.tvSettingContentChild.setText(aboutOptionsChild);
        }

        holder.itemView.setOnClickListener(v -> {
           if (position == 0) {

               Intent intent = new Intent(holder.parent.getContext(), UserUIActivity.class);
               holder.parent.getContext().startActivity(intent);
           }else {
                Intent intent = new Intent(holder.parent.getContext(), AboutActivity.class);
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
