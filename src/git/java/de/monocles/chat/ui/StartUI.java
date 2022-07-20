package de.monocles.chat.ui;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.util.IntroHelper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
public class StartUI extends AppCompatActivity {
    private static final int NeededPermissions = 1000;
    String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_ui);
        IntroHelper.showIntro(this, false);
        String PREF_FIRST_START = "FirstStart";
        SharedPreferences FirstStart = getApplicationContext().getSharedPreferences(PREF_FIRST_START, Context.MODE_PRIVATE);
        long FirstStartTime = FirstStart.getLong(PREF_FIRST_START, 0);
        Log.d(Config.LOGTAG, "All permissions granted, starting " + getString(R.string.app_name) + "(" + FirstStartTime + ")");
        Intent intent = new Intent(this, ConversationsActivity.class);
        intent.putExtra(PREF_FIRST_START, FirstStartTime);
        startActivity(intent);
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
        finish();    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}