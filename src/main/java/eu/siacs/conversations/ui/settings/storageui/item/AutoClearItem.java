package eu.siacs.conversations.ui.settings.storageui.item;

import java.io.Serializable;

public class AutoClearItem implements Serializable {

    private String title;
    private String size;

    public AutoClearItem(String title, String size){
        this.title = title;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getSize() {
        return size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
