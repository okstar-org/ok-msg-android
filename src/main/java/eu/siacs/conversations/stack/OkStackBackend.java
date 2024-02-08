package eu.siacs.conversations.stack;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.http.HttpConnectionManager;
import okhttp3.HttpUrl;

public class OkStackBackend {


    public static Res<AccountInfo> GetJid(String account) {
        try {
            String url = BuildConfig.OK_STACK_API_URL + "/open/passport/account/" + account;
            String json = HttpConnectionManager.getJSON(HttpUrl.get(url));
            return new Gson().fromJson(json, new TypeToken<Res<AccountInfo>>() {
            }.getType());
        } catch (IOException e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 读取服务器信息
     * @return
     */
    public static MeetInfo GetMeetInfo() {
        try {
            String url = BuildConfig.OK_STACK_API_URL + "/.well-known/meet.json";
            String json = HttpConnectionManager.getJSON(HttpUrl.get(url));
            return new Gson().fromJson(json, new TypeToken<MeetInfo>() {
            }.getType());
        } catch (IOException e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }
}
