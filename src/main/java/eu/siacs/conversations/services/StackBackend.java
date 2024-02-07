package eu.siacs.conversations.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.entities.AccountInfo;
import eu.siacs.conversations.http.HttpConnectionManager;
import eu.siacs.conversations.http.Res;
import okhttp3.HttpUrl;

public class StackBackend {


    public static Res<AccountInfo> GetJid(String account) {
        String url = BuildConfig.OK_STACK_API_URL + "/open/passport/account/" + account;
        String json = null;
        try {
            json = HttpConnectionManager.getJSON(HttpUrl.get(url));
        } catch (IOException e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
        Res<AccountInfo> res = new Gson().fromJson(json, new TypeToken<Res<AccountInfo>>() {
        }.getType());
        return res;
    }
}
