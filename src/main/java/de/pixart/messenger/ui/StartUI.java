package de.pixart.messenger.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.util.IntroHelper;

import eu.siacs.conversations.utils.Compatibility;
import eu.siacs.conversations.utils.ThemeHelper;

    public class StartUI extends PermissionsActivity
            implements PermissionsActivity.OnPermissionGranted {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_ui);
        setTheme(ThemeHelper.findDialog(this));
        IntroHelper.showIntro(this, false);
    }


    @Override
        protected void onStart() {
            super.onStart();
            requestNeededPermissions();
    }

        private void requestNeededPermissions() {
            if (Compatibility.runsTwentyThree()) {
                if (!checkStoragePermission()) {
                    requestStoragePermission(this);
                }
                if (Compatibility.runsAndTargetsThirty(this)) {
                    requestAllFilesAccess(this);
                }
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPermissionGranted() {
        next(this);
    }

    public static void next(final Activity activity) {
        String PREF_FIRST_START = "FirstStart";
        SharedPreferences FirstStart = activity.getSharedPreferences(PREF_FIRST_START, Context.MODE_PRIVATE);
        long FirstStartTime = FirstStart.getLong(PREF_FIRST_START, 0);
        Intent intent = new Intent(activity, ConversationsActivity.class);
        intent.putExtra(PREF_FIRST_START, FirstStartTime);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
        activity.finish();
    }
}