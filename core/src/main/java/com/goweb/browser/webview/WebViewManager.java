package com.goweb.browser.webview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.goweb.browser.R;
import com.goweb.browser.ui.dialog.CustomDialog;
import com.goweb.browser.utils.SettingsUtils;

import java.io.File;

public class WebViewManager {

    public static final int REQUEST_FILE_CHOOSER = 8888;

    public interface WebViewCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onProgressChanged(int newProgress);
        void onReceivedTitle(String title);
        void onReceivedIcon(Bitmap icon);
        void onError(int errorCode, String description, String failingUrl);
        void onDownloadStart(String url, String contentDisposition, String mimeType);
        void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback);
        void onHideCustomView();
        void onFileChooserRequested(ValueCallback<Uri[]> filePathCallback);
    }

    private WebView webView;
    private WebViewCallback callback;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private ValueCallback<Uri[]> filePathCallback;
    private ValueCallback<Uri> filePathCallbackLegacy;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public WebViewManager(Context context, WebViewCallback callback) {
        this.callback = callback;
        initWebView(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(Context context) {
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(SettingsUtils.isEnableJs());
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setSupportMultipleWindows(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSaveFormData(true);
        settings.setLoadsImagesAutomatically(!SettingsUtils.isBlockImages());
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setTextZoom(SettingsUtils.getFontSize());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        File cacheDir = new File(context.getCacheDir(), "webview_cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        settings.setDatabasePath(cacheDir.getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, SettingsUtils.isEnableCookies());
            cookieManager.setAcceptCookie(SettingsUtils.isEnableCookies());
        }

        applyUserAgent(settings);

        webView.setWebViewClient(createWebViewClient());
        webView.setWebChromeClient(createWebChromeClient());
        webView.setDownloadListener(createDownloadListener());

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();

        webView.setScrollbarFadingEnabled(true);
        webView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

        clearOldWebviewData(context);
    }

    private void clearOldWebviewData(Context context) {
        try {
            File cacheDir = new File(context.getCacheDir(), "webview_cache");
            if (cacheDir.exists()) {
                long maxSize = 30 * 1024 * 1024;
                long size = getDirSize(cacheDir);
                if (size > maxSize) {
                    deleteDirContents(cacheDir);
                }
            }
        } catch (Exception e) {
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) size += getDirSize(f);
            else size += f.length();
        }
        return size;
    }

    private void deleteDirContents(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDirContents(f);
            f.delete();
        }
    }

    private void applyUserAgent(WebSettings settings) {
        int uaMode = SettingsUtils.getUaMode();
        switch (uaMode) {
            case SettingsUtils.UA_PC:
                settings.setUserAgentString(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                break;
            case SettingsUtils.UA_CUSTOM:
                String customUa = SettingsUtils.getCustomUa();
                if (customUa != null && !customUa.isEmpty()) {
                    settings.setUserAgentString(customUa);
                }
                break;
            default:
                break;
        }
    }

    private WebViewClient createWebViewClient() {
        return new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return handleUrlLoading(view, request.getUrl().toString());
                }
                return false;
            }

            private boolean handleUrlLoading(WebView view, String url) {
                if (url == null) return false;
                if (url.startsWith("http://") || url.startsWith("https://") ||
                        url.startsWith("file:///") || url.startsWith("data:") ||
                        url.startsWith("javascript:")) {
                    return false;
                }
                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            view.getContext().startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                    }
                    return true;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (callback != null) {
                    callback.onPageStarted(url);
                    if (favicon != null) callback.onReceivedIcon(favicon);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (callback != null) callback.onPageFinished(url);
                if (SettingsUtils.isNightMode()) injectNightMode(view);
                if (SettingsUtils.isEnableCookies()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().flush();
                    } else {
                        CookieSyncManager.getInstance().sync();
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (callback != null) callback.onError(errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (request.isForMainFrame()) {
                        if (callback != null) {
                            callback.onError(error.getErrorCode(),
                                    error.getDescription() != null ? error.getDescription().toString() : "",
                                    request.getUrl().toString());
                        }
                    }
                }
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                Context ctx = webView != null ? webView.getContext() : null;
                if (ctx instanceof Activity) {
                    final Activity activity = (Activity) ctx;
                    if (activity.isFinishing()) {
                        handler.cancel();
                        return;
                    }
                    activity.runOnUiThread(() -> {
                        try {
                            new CustomDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.ssl_error_title))
                                    .setMessage(activity.getString(R.string.ssl_error_message))
                                    .setPositiveButton(activity.getString(R.string.btn_continue), handler::proceed)
                                    .setNegativeButton(activity.getString(R.string.cancel), handler::cancel)
                                    .show();
                        } catch (Exception e) {
                            handler.cancel();
                        }
                    });
                } else {
                    handler.cancel();
                }
            }

            @Override
            public void onFormResubmission(WebView view, final android.os.Message dontResend, final android.os.Message resend) {
                Context ctx = webView != null ? webView.getContext() : null;
                if (ctx instanceof Activity) {
                    final Activity activity = (Activity) ctx;
                    activity.runOnUiThread(() -> {
                        try {
                            new CustomDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.confirm_resubmission_title))
                                    .setMessage(activity.getString(R.string.confirm_resubmission_message))
                                    .setPositiveButton(activity.getString(R.string.btn_resend), () -> resend.sendToTarget())
                                    .setNegativeButton(activity.getString(R.string.cancel), () -> dontResend.sendToTarget())
                                    .show();
                        } catch (Exception e) {
                            dontResend.sendToTarget();
                        }
                    });
                } else {
                    dontResend.sendToTarget();
                }
            }

            private void injectNightMode(WebView view) {
                String js = "javascript:(function(){" +
                        "var s=document.createElement('style');s.type='text/css';" +
                        "s.id='goweb-night-mode';" +
                        "if(document.getElementById('goweb-night-mode'))return;" +
                        "s.innerHTML='html{background-color:#121212!important;}" +
                        "body{background-color:#121212!important;color:#e0e0e0!important;}" +
                        "a{color:#52b788!important;}a:visited{color:#40916c!important;}" +
                        "input,textarea,select,button{background-color:#2d2d2d!important;color:#e0e0e0!important;border-color:#555!important;}" +
                        "table,th,td{border-color:#444!important;}" +
                        "img{opacity:0.85!important;}" +
                        "div,section,article,aside,header,footer,nav,main{background-color:transparent!important;color:#e0e0e0!important;}" +
                        "h1,h2,h3,h4,h5,h6{color:#e0e0e0!important;}" +
                        "p,span,li,dd,dt{color:#d0d0d0!important;}" +
                        "pre,code{background-color:#1a1a1a!important;color:#d4d4d4!important;}" +
                        "blockquote{border-color:#52b788!important;}" +
                        "::-webkit-scrollbar{width:6px;}" +
                        "::-webkit-scrollbar-track{background:#1a1a1a;}" +
                        "::-webkit-scrollbar-thumb{background:#444;border-radius:3px;}';" +
                        "document.head.appendChild(s);" +
                        "})()";
                try {
                    view.loadUrl(js);
                } catch (Exception e) {
                }
            }
        };
    }

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (callback != null) callback.onProgressChanged(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (callback != null) callback.onReceivedTitle(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                if (callback != null) callback.onReceivedIcon(icon);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback geoCallback) {
                Context ctx = webView != null ? webView.getContext() : null;
                if (ctx instanceof Activity) {
                    final Activity activity = (Activity) ctx;
                    activity.runOnUiThread(() -> {
                        try {
                            new CustomDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.location_access_title))
                                    .setMessage(activity.getString(R.string.location_access_message, origin))
                                    .setPositiveButton(activity.getString(R.string.btn_allow), () -> geoCallback.invoke(origin, true, false))
                                    .setNegativeButton(activity.getString(R.string.btn_deny), () -> geoCallback.invoke(origin, false, false))
                                    .show();
                        } catch (Exception e) {
                            geoCallback.invoke(origin, false, false);
                        }
                    });
                } else {
                    geoCallback.invoke(origin, true, false);
                }
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String[] resources = request.getResources();
                    boolean hasCamera = false;
                    boolean hasMic = false;
                    for (String r : resources) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) hasCamera = true;
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) hasMic = true;
                    }
                    if (hasCamera || hasMic) {
                        Context ctx = webView != null ? webView.getContext() : null;
                        if (ctx instanceof Activity) {
                            Activity activity = (Activity) ctx;
                            if (Build.VERSION.SDK_INT >= 23) {
                                boolean hasPermission = true;
                                if (hasCamera && activity.checkSelfPermission(Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    hasPermission = false;
                                }
                                if (hasMic && activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    hasPermission = false;
                                }
                                if (!hasPermission) {
                                    String[] perms = {};
                                    if (hasCamera) perms = new String[]{Manifest.permission.CAMERA};
                                    if (hasMic) perms = new String[]{Manifest.permission.RECORD_AUDIO};
                                    if (hasCamera && hasMic) {
                                        perms = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
                                    }
                                    activity.requestPermissions(perms, 9999);
                                    request.deny();
                                    return;
                                }
                            }
                        }
                    }
                    request.grant(request.getResources());
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback cb) {
                if (customView != null) {
                    cb.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = cb;
                if (callback != null) callback.onShowCustomView(view, cb);
            }

            @Override
            public void onHideCustomView() {
                if (customViewCallback != null) {
                    try {
                        customViewCallback.onCustomViewHidden();
                    } catch (Exception e) {
                    }
                }
                customView = null;
                customViewCallback = null;
                if (callback != null) callback.onHideCustomView();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                WebViewManager.this.filePathCallback = filePathCallback;
                if (callback != null) {
                    callback.onFileChooserRequested(filePathCallback);
                }
                return true;
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                filePathCallbackLegacy = uploadMsg;
                if (callback != null) {
                    callback.onFileChooserRequested(null);
                }
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackLegacy = uploadMsg;
                if (callback != null) {
                    callback.onFileChooserRequested(null);
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                                          android.os.Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                String url = result.getExtra();
                if (url != null && !url.isEmpty()) {
                    view.loadUrl(url);
                }
                return false;
            }
        };
    }

    private DownloadListener createDownloadListener() {
        return (url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (callback != null) callback.onDownloadStart(url, contentDisposition, mimetype);
        };
    }

    public void handleFileChooserResult(int resultCode, Intent data) {
        if (filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                ClipData clipData = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    clipData = data.getClipData();
                }
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        results[i] = clipData.getItemAt(i).getUri();
                    }
                } else if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
        if (filePathCallbackLegacy != null) {
            Uri result = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                result = data.getData();
            }
            filePathCallbackLegacy.onReceiveValue(result);
            filePathCallbackLegacy = null;
        }
    }

    public void cancelFileChooser() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (filePathCallbackLegacy != null) {
            filePathCallbackLegacy.onReceiveValue(null);
            filePathCallbackLegacy = null;
        }
    }

    public WebView getWebView() {
        return webView;
    }

    public void loadUrl(String url) {
        if (webView != null && url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    public void goBack() {
        if (webView != null && webView.canGoBack()) webView.goBack();
    }

    public void goForward() {
        if (webView != null && webView.canGoForward()) webView.goForward();
    }

    public void reload() {
        if (webView != null) webView.reload();
    }

    public void stopLoading() {
        if (webView != null) webView.stopLoading();
    }

    public String getUrl() {
        return webView != null ? webView.getUrl() : "";
    }

    public String getTitle() {
        return webView != null ? webView.getTitle() : "";
    }

    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    public boolean canGoForward() {
        return webView != null && webView.canGoForward();
    }

    public void clearCache(boolean includeDiskFiles) {
        if (webView != null) webView.clearCache(includeDiskFiles);
    }

    public void clearHistory() {
        if (webView != null) webView.clearHistory();
    }

    public void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
    }

    public void clearFormData() {
        if (webView != null) webView.clearFormData();
    }

    public void clearAllData() {
        clearCache(true);
        clearHistory();
        clearCookies();
        clearFormData();
        try {
            Context ctx = webView != null ? webView.getContext() : null;
            if (ctx != null) {
                File webviewCache = new File(ctx.getCacheDir(), "webview_cache");
                if (webviewCache.exists()) deleteDirContents(webviewCache);
            }
        } catch (Exception e) {
        }
    }

    public void onPause() {
        if (webView != null) webView.onPause();
    }

    public void onResume() {
        if (webView != null) webView.onResume();
    }

    public void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        cancelFileChooser();
    }

    public void updateSettings() {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(SettingsUtils.isEnableJs());
        settings.setBlockNetworkImage(SettingsUtils.isBlockImages());
        settings.setTextZoom(SettingsUtils.getFontSize());
        applyUserAgent(settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, SettingsUtils.isEnableCookies());
        }
    }

    public boolean isInCustomView() {
        return customView != null;
    }

    public void hideCustomView() {
        if (customViewCallback != null) {
            try {
                customViewCallback.onCustomViewHidden();
            } catch (Exception e) {
            }
        }
        customView = null;
        customViewCallback = null;
    }

    public String getCookiesForUrl(String url) {
        try {
            return CookieManager.getInstance().getCookie(url);
        } catch (Exception e) {
            return "";
        }
    }
}
