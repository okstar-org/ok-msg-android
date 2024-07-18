package eu.siacs.conversations.ui.settings.userui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.utils.ThemeHelper;

/**
 * 用戶ui界面
 */
public class UserUIActivity extends XmppActivity {

    @Override
    protected void refreshUiReal() {

    }

    @Override
    protected void onBackendConnected() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeHelper.find(this));
        setContentView(R.layout.activity_user_ui);
        setSupportActionBar(findViewById(R.id.toolbar));
        configureActionBar(getSupportActionBar());

    }
}