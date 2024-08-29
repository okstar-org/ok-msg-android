package eu.siacs.conversations.cloud;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.http.HttpConnectionManager;
import okhttp3.HttpUrl;

public class OkCloudBackend {


    public static FederalInfo GetFederalInfo() {
        try {
            //username
            String url = BuildConfig.OK_CLOUD_API_URL + "/open/federal/.well-known/info.json";
            String json = HttpConnectionManager.getJSON(HttpUrl.get(url));
            return new Gson().fromJson(json, FederalInfo.class);
        } catch (IOException e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }


}
