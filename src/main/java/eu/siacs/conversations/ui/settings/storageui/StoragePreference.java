package eu.siacs.conversations.ui.settings.storageui;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class StoragePreference extends Preference {

    public StoragePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StoragePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StoragePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onClick() {
        super.onClick();
        final Intent intent = new Intent(getContext(), StorageActivity.class);
        getContext().startActivity(intent);
    }
}
