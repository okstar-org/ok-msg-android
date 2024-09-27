package org.okstar.okmsg.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.mmkv.MMKV;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.util.IntroHelper;

public class StartUI extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_ui);
        IntroHelper.showIntro(this, false);
        String PREF_FIRST_START = "FirstStart";
        SharedPreferences FirstStart = getApplicationContext().getSharedPreferences(PREF_FIRST_START, Context.MODE_PRIVATE);
        MMKV.initialize(this);
        long FirstStartTime = FirstStart.getLong(PREF_FIRST_START, 0);
        Log.d(Config.LOGTAG, "Starting " + getString(R.string.app_name) + "(" + FirstStartTime + ")");
        new Handler().postDelayed(()->{
            Intent intent = new Intent(this, ConversationsActivity.class);
            intent.putExtra(PREF_FIRST_START, FirstStartTime);
            startActivity(intent);
            overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
            finish();
        },1000);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}