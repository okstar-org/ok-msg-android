package eu.siacs.conversations.ui.settings.storageui;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.settings.storageui.item.StorageItem;

public class StorageViewHolder extends RecyclerView.ViewHolder {

    private CheckBox cbSelectFile;
    private TextView tvShowTitle;
    private TextView tvShowProportion;
    private TextView tvShowValueCount;

    public StorageViewHolder(@NonNull View itemView) {
        super(itemView);
        initView(itemView);
    }

    private void initView(View itemView) {
        cbSelectFile = itemView.findViewById(R.id.checkbox_select_file_storage);
        tvShowTitle = itemView.findViewById(R.id.tv_show_title_storage);
        tvShowProportion = itemView.findViewById(R.id.tv_show_proportion_storage);
        tvShowValueCount = itemView.findViewById(R.id.tv_show_value_count_storage);

    }

    public void setStorageItem(StorageItem storageItem){
        cbSelectFile.setChecked(storageItem.isSelected());
        tvShowTitle.setText(storageItem.getTitle());
        tvShowProportion.setText(storageItem.getProportion());
        tvShowValueCount.setText(storageItem.getValueCount());


        cbSelectFile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storageItem.setSelected(isChecked);
        });
    }

}
