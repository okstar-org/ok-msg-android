package org.okstar.okmsg.stack;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.cloud.FederalInfo;
import eu.siacs.conversations.entities.SignUpForm;
import eu.siacs.conversations.entities.SignUpResult;
import eu.siacs.conversations.http.HttpConnectionManager;
import okhttp3.HttpUrl;

public class OkStackBackend {
    String baseUrl;

    private OkStackBackend(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static OkStackBackend Get(FederalInfo.State state) {
        return new OkStackBackend(state.getStackUrl());
    }

    public static OkStackBackend Get(String stackUrl) {
        return new OkStackBackend(stackUrl);
    }

    /**
     * 注册
     *
     * @param email
     * @param password
     * @return
     */
    public Res<SignUpResult> signUp(String email, String password) {
        try {
            HttpUrl url = HttpUrl.get(baseUrl + "/api/auth/passport/signUp");

            SignUpForm signUp = new SignUpForm();
            signUp.setLanguage("zh-CN");
            signUp.setIso("CN");
            signUp.setAccountType("email");
            signUp.setAccount(email);
            signUp.setPassword(password);

            String post = HttpConnectionManager.postJSON(url, signUp);
            return new Gson().fromJson(post, new TypeToken<Res<SignUpResult>>() {
            }.getType());
        } catch (Exception e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }

    public Res<AccountInfo> getJid(String account) {
        try {
            // Expected URL scheme 'http' or 'https'
            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")){
                baseUrl = "http://" + baseUrl;
            }
            String url = baseUrl + "/api/open/passport/account/" + account;
            String json = HttpConnectionManager.getJSON(HttpUrl.get(url));
            return new Gson().fromJson(json, new TypeToken<Res<AccountInfo>>() {
            }.getType());
        } catch (Exception e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 读取服务器信息
     *
     * @return
     */
    public MeetInfo getMeetInfo() {
        try {
            String url0 = baseUrl + "/api/open/.well-known/meet.json";
            String json = HttpConnectionManager.getJSON(HttpUrl.get(url0));
            return new Gson().fromJson(json, new TypeToken<MeetInfo>() {
            }.getType());
        } catch (IOException e) {
            Log.e(Config.LOGTAG, e.getMessage(), e);
            return null;
        }
    }
}
