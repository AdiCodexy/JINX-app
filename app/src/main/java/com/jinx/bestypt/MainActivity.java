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

    private static final String APP_URL = "https://adicodexy.github.io/BetterThanYpt/";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#0a0a0f"));

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0a0a0f"));

        // ── WebView ──
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0a0a0f"));
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        // ── Key fix: add BestYptApp to UA so HTML detects app context ──
        // and uses signInWithRedirect instead of signInWithPopup
        String baseUA = ws.getUserAgentString();
        ws.setUserAgentString(baseUA + " BestYptApp/1.0");

        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        // ── Progress bar ──
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(
            android.content.res.ColorStateList.valueOf(Color.parseColor("#6c5ce7")));
        root.addView(progressBar, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8));

        // ── Error layout ──
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
        errMsg.setText("Could not load JINX.\nCheck your internet and try again.");
        errMsg.setTextColor(Color.parseColor("#6b6b80"));
        errMsg.setTextSize(14);
        errMsg.setGravity(android.view.Gravity.CENTER);
        errMsg.setPadding(0, 16, 0, 40);
        errorLayout.addView(errMsg);

        Button retryBtn = new Button(this);
        retryBtn.setText("Try Again");
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setBackgroundColor(Color.parseColor("#6c5ce7"));
        retryBtn.setPadding(48, 24, 48, 24);
        retryBtn.setOnClickListener(v -> loadApp());
        errorLayout.addView(retryBtn);

        root.addView(errorLayout, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);

        // ── WebViewClient ──
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    String url = req.getUrl().toString();
                    // Never show error for auth pages — they redirect
                    if (url.contains("accounts.google.com") ||
                        url.contains("googleapis.com") ||
                        url.contains("firebaseapp.com")) return;
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                // Keep ALL of these inside the WebView
                // Firebase redirect sign-in goes:
                // app → accounts.google.com → firebaseapp.com/__/auth/handler → app
                if (url.contains("adicodexy.github.io") ||
                    url.contains("accounts.google.com") ||
                    url.contains("googleapis.com") ||
                    url.contains("google.com/o/oauth") ||
                    url.contains("firebaseapp.com") ||
                    url.contains("firebase.google.com") ||
                    url.contains("ariarohiatre") ||
                    url.startsWith("about:")) {
                    return false; // load in WebView
                }
                // Open external links in browser
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception e) { /* ignore */ }
                return true;
            }
        });

        // ── WebChromeClient (progress only — no popup needed with redirect flow) ──
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                progressBar.setProgress(p);
                if (p == 100) progressBar.setVisibility(View.GONE);
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
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onResume()  { super.onResume();  webView.onResume(); }
    @Override protected void onPause()   { super.onPause();   webView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); webView.destroy(); }
}
