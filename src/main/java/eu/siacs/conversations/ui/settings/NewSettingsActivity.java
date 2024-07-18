package eu.siacs.conversations.ui.settings;

import android.os.Bundle;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.XmppActivity;

public class NewSettingsActivity extends XmppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_settings);
        findViewById(R.id.settings_list);


    }

    @Override
    protected void refreshUiReal() {

    }

    @Override
    protected void onBackendConnected() {

    }




}