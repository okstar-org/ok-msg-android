package eu.siacs.conversations.ui.settings.storageui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.settings.storageui.item.AutoClearItem;
import eu.siacs.conversations.ui.settings.storageui.item.StorageItem;

public class AutoClearAdapter extends RecyclerView.Adapter<AutoClearViewHolder> {

    private ArrayList<AutoClearItem> autoClearData = new ArrayList<>();

    @NonNull
    @Override
    public AutoClearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AutoClearViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto_clear_cache, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AutoClearViewHolder holder, int position) {
        holder.setStorageItem(autoClearData.get(position));
    }

    @Override
    public int getItemCount() {
        if (autoClearData.isEmpty()) {
            return 0;
        }
        return autoClearData.size();
    }

    public void addStorageItem(AutoClearItem item) {
        this.autoClearData.add(item);
        notifyDataSetChanged();
    }

    public void addStorageDataAll(ArrayList<AutoClearItem> items) {
        this.autoClearData.clear();
        this.autoClearData.addAll(items);
        notifyDataSetChanged();
    }
}
