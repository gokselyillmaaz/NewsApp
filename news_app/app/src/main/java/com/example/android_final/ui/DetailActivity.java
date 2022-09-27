package com.example.android_final.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.android_final.R;

public class DetailActivity extends Activity {

    private WebView detailWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailWebView =findViewById(R.id.detailWebView);
        WebSettings webSettings = detailWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        detailWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        Intent detailIntent = getIntent();
        if (detailIntent != null){
            String detailUrl = detailIntent.getStringExtra(MainActivity.DETAIL_INTENT_KEY);
            if(!detailUrl.isEmpty()){
                detailWebView.loadUrl(detailUrl);
            }
        }
    }
}