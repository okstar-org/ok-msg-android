package de.monocles.chat.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.common.base.Stopwatch;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.persistance.DatabaseBackend;
import eu.siacs.conversations.services.XmppConnectionService;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isRunning(XmppConnectionService.class)) {
            new optimizeDB(this).execute();
        } else {
            requestNeededPermissions();
        }
        IntroHelper.showIntro(this, false);
    }

    private void requestNeededPermissions() {
        if (Compatibility.runsTwentyThree()) {
            if (!checkStoragePermission()) {
                requestStoragePermission(this);
            }
            if (checkStoragePermission()) {
                next(this);
            }
        } else {
            next(this);
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


    private boolean isRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public class optimizeDB extends AsyncTask<Void, Void, Void> {

        StartUI activity;
        AlertDialog.Builder dialog;

        public optimizeDB(StartUI startUI) {
            activity = startUI;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(activity);
            dialog.setTitle(getString(R.string.app_name));
            dialog.setMessage(R.string.optimize_database);
            dialog.setCancelable(false);
            dialog.create().show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseBackend mDatabaseBackend = DatabaseBackend.getInstance(getBaseContext());
            Log.d(Config.LOGTAG, "Optimizing database");
            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                final SQLiteDatabase db = mDatabaseBackend.getWritableDatabase();
                db.execSQL("ANALYZE");
                db.execSQL("VACUUM");
                db.execSQL("PRAGMA optimize");
                Log.d(Config.LOGTAG, String.format("Optimized database in %s", stopwatch.stop()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDatabaseBackend.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            requestNeededPermissions();
        }
    }
}