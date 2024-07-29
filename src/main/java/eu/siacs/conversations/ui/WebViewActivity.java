package eu.siacs.conversations.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebSettings;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.util.HashSet;

import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.ActivityWebBinding;

public class WebViewActivity extends XmppActivity {
    private ActivityWebBinding binding;


    @Override
    protected void refreshUiReal() {
    }

    @Override
    protected void onBackendConnected() {
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);
        Toolbar toolbar = (Toolbar) binding.toolbar;
        setSupportActionBar(toolbar);
        configureActionBar(getSupportActionBar());

        webViewLoading("https://www.baidu.com/");
    }


    public static void launch(Context activity) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        activity.startActivity(intent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webViewLoading(String remoteUrl) {
        String url = remoteUrl;
        if (!isNetworkAvailable()) {
            //1. asset目录下的index.html文件
            url = "file:///android_asset/html/index.html";
            /*
            //2.本地内存中的index.html文件
            // 获取文件夹路径
            String htmlPath = getExternalFilesDir("html").getPath();
            File htmlFile = new File(htmlPath);
            // 判断是否存在，不存在则创建
            if (htmlFile.exists()) {
                htmlPath = htmlFile.getPath() + File.separator + "index.html";
            } else {
                htmlFile.mkdirs();
                htmlPath = htmlFile.getPath() + File.separator + "index.html";
            }
            // 地址
            String localFilePath = "file:///" + htmlPath;
             */
            //3.指定的URL的html文件
            //若是不显示，在AndroidManifest.xml中添加android:usesCleartextTraffic="true"
            //并且设置网络权限
            //String urlPath = "https://www.baidu.com/";
        }


        WebSettings myWebSettings = binding.webView.getSettings();
        // webView解决加载html页面空白问题
        myWebSettings.setJavaScriptEnabled(true);// 设置支持javascript
        myWebSettings.setUseWideViewPort(true);//将图片调整到适合webView大小
        myWebSettings.setLoadWithOverviewMode(true);//缩放至屏幕大小
        myWebSettings.setDomStorageEnabled(true);//设置DOM缓存，当H5网页使用localstorage时一定要设置
        myWebSettings.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);// 设置去缓存，防止加载的是上一次数据
        myWebSettings.setDatabaseEnabled(true);

        // 解决加载本地内存中报错 err_access_denied
        myWebSettings.setAllowFileAccess(true);
        myWebSettings.setAllowContentAccess(true);

        // 解决webView报错 Loading local files from file:// urls is not possible due browser security restrictions
        /**
         *  设置是否允许运行在一个file schema URL环境下的JavaScript访问来自其他任何来源的内容，
         * 包括其他file schema URLs。
         * 通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，
         * 包括其他的文件和http,https等其他的源。与上面的类似，实现一个就可以。
         * webSetting.setAllowUniversalAccessFromFileURLs(true);
         * */
        myWebSettings.setAllowUniversalAccessFromFileURLs(true);
        /**
         * 设置是否允许运行在一个file schema URL环境下的JavaScript访问来自其他任何来源的内容，
         * 包括其他file schema URLs。
         * 通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，
         * 包括其他的文件和http,https等其他的源。与上面的类似，实现一个就可以。
         */
        //myWebSettings.setAllowUniversalAccessFromFileURLs(true);


        //加载html
        binding.webView.loadUrl(url);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

