package eu.siacs.conversations.ui.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import eu.siacs.conversations.R;

public class NewSettingsViewHolder extends RecyclerView.ViewHolder{

    public ViewGroup parent;
    public AppCompatTextView tvSettingsContentFirst;
    public AppCompatTextView tvSettingContentParent;
    public AppCompatTextView tvSettingContentChild;

    public NewSettingsViewHolder(@NonNull ViewGroup parent, @NonNull View itemView) {
        super(itemView);
        this.parent = parent;
        initView(itemView);
    }

    private void initView(View itemView){
        tvSettingsContentFirst = itemView.findViewById(R.id.tv_settings_content_first);
        tvSettingContentParent = itemView.findViewById(R.id.tv_settings_content_parent);
        tvSettingContentChild = itemView.findViewById(R.id.tv_settings_content_child);
    }

}
