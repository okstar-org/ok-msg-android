package eu.siacs.conversations.ui.settings.storageui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.settings.storageui.item.StorageItem;

public class StorageAdapter extends RecyclerView.Adapter<StorageViewHolder>{

    private final ArrayList<StorageItem> storageList = new ArrayList<>();

    public StorageAdapter(){

    }


    @NonNull
    @Override
    public StorageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StorageViewHolder(LayoutInflater.
                from(parent.getContext()).inflate(R.layout.item_storage_settings, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StorageViewHolder holder, int position) {
        StorageItem item = storageList.get(position);
        holder.setStorageItem(item);
    }

    @Override
    public int getItemCount() {
        if (storageList.isEmpty()) {
            return 0;
        }
        return storageList.size();
    }

    public void addStorageItem(StorageItem item) {
        this.storageList.add(item);
        notifyDataSetChanged();
    }

    public void addStorageDataAll(ArrayList<StorageItem> items) {
        this.storageList.clear();
        this.storageList.addAll(items);
        notifyDataSetChanged();
    }

    public ArrayList<StorageItem> getStorageItems() {
        return storageList;
    }

}
