package eu.siacs.conversations.ui.settings.storageui.item;

import java.io.Serializable;

public class StorageItem implements Serializable {

    private String title;
    private String proportion;
    private String valueCount;
    private boolean isSelected;

    public StorageItem(String title, String proportion, String valueCount, boolean isSelected) {
        this.title = title;
        this.proportion = proportion;
        this.valueCount = valueCount;
        this.isSelected = isSelected;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getProportion() {
        return proportion;
    }

    public String getValueCount() {
        return valueCount;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setValueCount(String valueCount) {
        this.valueCount = valueCount;
    }

    public void setProportion(String proportion) {
        this.proportion = proportion;
    }
}
