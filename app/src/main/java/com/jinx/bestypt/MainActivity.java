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

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0a0a0f"));
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setSupportMultipleWindows(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36");

        root.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(
            android.content.res.ColorStateList.valueOf(Color.parseColor("#6c5ce7")));
        root.addView(progressBar, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 8));

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

        // Main WebView client — let everything through, never show error for auth pages
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
                // If we land on a blank/grey Firebase auth handler page,
                // redirect back to the app immediately
                if (url.contains("firebaseapp.com/__/auth") ||
                    url.contains("/__/auth/handler")) {
                    // Let Firebase JS finish its work then go home
                    view.evaluateJavascript(
                        "(function(){ setTimeout(function(){ " +
                        "  if(document.body && document.body.innerHTML.trim().length < 50){" +
                        "    window.location.href='" + APP_URL + "';" +
                        "  }" +
                        "}, 2000); })();", null);
                }
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    String url = req.getUrl().toString();
                    // Never show error screen for auth-related pages
                    if (url.contains("accounts.google.com") ||
                        url.contains("googleapis.com") ||
                        url.contains("firebaseapp.com") ||
                        url.contains("firebase.google.com")) {
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                // Keep ALL of these inside the WebView
                if (url.contains("adicodexy.github.io") ||
                    url.contains("accounts.google.com") ||
                    url.contains("googleapis.com") ||
                    url.contains("google.com/o/oauth") ||
                    url.contains("firebaseapp.com") ||
                    url.contains("firebase.google.com") ||
                    url.contains("ariarohiatre") ||
                    url.startsWith("about:")) {
                    return false;
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception e) { /* ignore */ }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                progressBar.setProgress(p);
                if (p == 100) progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                    boolean isUserGesture, android.os.Message resultMsg) {
                // Popup WebView for Google sign-in
                final WebView popup = new WebView(MainActivity.this);
                WebSettings ps = popup.getSettings();
                ps.setJavaScriptEnabled(true);
                ps.setDomStorageEnabled(true);
                ps.setUserAgentString(
                    "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Mobile Safari/537.36");

                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView v, String url) {
                        // Once Firebase auth handler finishes, close popup + reload app
                        if (url.contains("firebaseapp.com/__/auth") ||
                            url.contains("/__/auth/handler")) {
                            // Give Firebase JS 1.5s to post the token back to the opener
                            popup.postDelayed(new Runnable() {
                                @Override public void run() {
                                    root.removeView(popup);
                                    // Reload the app so onAuthStateChanged fires
                                    webView.loadUrl(APP_URL);
                                }
                            }, 1500);
                        }
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        String url = req.getUrl().toString();
                        // If redirected back to the app directly, load in main WebView
                        if (url.contains("adicodexy.github.io")) {
                            root.removeView(popup);
                            webView.loadUrl(url);
                            return true;
                        }
                        return false;
                    }
                });

                popup.setWebChromeClient(new WebChromeClient());

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
                root.addView(popup, lp);

                WebView.WebViewTransport t = (WebView.WebViewTransport) resultMsg.obj;
                t.setWebView(popup);
                resultMsg.sendToTarget();
                return true;
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
