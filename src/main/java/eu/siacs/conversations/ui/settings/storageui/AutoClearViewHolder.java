package eu.siacs.conversations.ui.settings.storageui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.settings.storageui.item.AutoClearItem;

public class AutoClearViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivShowMsgType;
    private TextView tvShowMsgContent;
    private TextView tvShowMsgSize;

    public AutoClearViewHolder(@NonNull View itemView) {
        super(itemView);
        ivShowMsgType = itemView.findViewById(R.id.iv_show_msg_type_auto);
        tvShowMsgContent = itemView.findViewById(R.id.tv_show_msg_content_auto);
        tvShowMsgSize = itemView.findViewById(R.id.tv_show_msg_size_auto);

    }

    public void setStorageItem(AutoClearItem storageItem) {
          ivShowMsgType.setImageResource(R.drawable.star);
          tvShowMsgContent.setText(storageItem.getTitle());
          tvShowMsgSize.setText(storageItem.getSize());

    }




}
