package com.jinx.bestypt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {

    // ── CHANGE THIS to your GitHub Pages URL ──
    private static final String APP_URL = "https://aditya7karale.github.io/bestypt/";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private boolean loadedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen dark background while loading
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#0a0a0f"));

        // Root layout
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0a0a0f"));

        // WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0a0a0f"));
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setUserAgentString(ws.getUserAgentString() + " BestYptApp/1.0");
        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // Progress bar (accent purple)
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6c5ce7")));
        progressBar.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams pbParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8);
        pbParams.topMargin = 0;
        root.addView(progressBar, pbParams);

        // Error layout (shown when load fails)
        errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(android.view.Gravity.CENTER);
        errorLayout.setBackgroundColor(Color.parseColor("#0a0a0f"));
        errorLayout.setVisibility(View.GONE);
        errorLayout.setPadding(64, 64, 64, 64);

        TextView errTitle = new TextView(this);
        errTitle.setText("Error — JINX messed up");
        errTitle.setTextColor(Color.parseColor("#f0f0f5"));
        errTitle.setTextSize(22);
        errTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        errTitle.setGravity(android.view.Gravity.CENTER);
        errorLayout.addView(errTitle);

        TextView errMsg = new TextView(this);
        errMsg.setText("Could not load Best Ypt Group.\nCheck your internet connection or try again later.");
        errMsg.setTextColor(Color.parseColor("#6b6b80"));
        errMsg.setTextSize(14);
        errMsg.setGravity(android.view.Gravity.CENTER);
        errMsg.setPadding(0, 16, 0, 40);
        errMsg.setLineSpacing(6, 1);
        errorLayout.addView(errMsg);

        Button retryBtn = new Button(this);
        retryBtn.setText("Try Again");
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setTextSize(15);
        retryBtn.setBackgroundColor(Color.parseColor("#6c5ce7"));
        retryBtn.setPadding(48, 28, 48, 28);
        retryBtn.setOnClickListener(v -> loadApp());
        errorLayout.addView(retryBtn);

        Button openBtn = new Button(this);
        openBtn.setText("Open in Browser");
        openBtn.setTextColor(Color.parseColor("#6b6b80"));
        openBtn.setTextSize(13);
        openBtn.setBackgroundColor(Color.TRANSPARENT);
        openBtn.setPadding(48, 16, 48, 16);
        openBtn.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(APP_URL)));
        });
        errorLayout.addView(openBtn);

        root.addView(errorLayout, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);

        // WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadedOnce = true;
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                    WebResourceError error) {
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                // Keep internal navigation inside the WebView
                if (url.startsWith(APP_URL) || url.contains("aditya7karale.github.io")) {
                    return false;
                }
                // Open external links in browser
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });

        // Progress via WebChromeClient
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) progressBar.setVisibility(View.GONE);
            }
        });

        loadApp();
    }

    private void loadApp() {
        errorLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(APP_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}
