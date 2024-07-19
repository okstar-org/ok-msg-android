package eu.siacs.conversations.ui.settings;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.utils.ThemeHelper;

public class NewSettingsActivity extends XmppActivity {

    private RecyclerView settingsRecyclerview;
    private NewSettingsAdapter newSettingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeHelper.find(this));
        ThemeHelper.applyCustomColors(this);
        setContentView(R.layout.activity_new_settings);
        getWindow().getDecorView().setBackgroundColor(StyledAttributes.getColor(this, R.attr.color_background_secondary));
        setSupportActionBar(findViewById(R.id.toolbar));
        configureActionBar(getSupportActionBar());

        settingsRecyclerview = findViewById(R.id.settings_list);

        settingsRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        newSettingsAdapter = new NewSettingsAdapter();
        newSettingsAdapter.setItem(getString(R.string.pref_ui_options));
        newSettingsAdapter.setItem(getString(R.string.title_activity_about));
        settingsRecyclerview.setAdapter(newSettingsAdapter);
    }

    @Override
    protected void refreshUiReal() {

    }

    @Override
    protected void onBackendConnected() {

    }




}