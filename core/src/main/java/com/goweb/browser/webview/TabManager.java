package com.goweb.browser.webview;

import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.goweb.browser.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.List;

public class TabManager {

    public static class Tab {
        public long id;
        public String title;
        public String url;
        public android.graphics.Bitmap favicon;
        public WebViewManager webViewManager;

        public Tab(long id) {
            this.id = id;
            this.title = "";
            this.url = "";
        }
    }

    private Context context;
    private FrameLayout container;
    private WebViewManager.WebViewCallback callback;
    private List<Tab> tabs = new ArrayList<>();
    private int activeIndex = -1;
    private long nextId = 1;

    public TabManager(Context context, FrameLayout container, WebViewManager.WebViewCallback callback) {
        this.context = context;
        this.container = container;
        this.callback = callback;
    }

    public Tab createTab(String url) {
        Tab tab = new Tab(nextId++);
        tab.webViewManager = new WebViewManager(context, callback);
        tabs.add(tab);
        switchToTab(tabs.size() - 1);
        if (url != null && !url.isEmpty()) {
            tab.webViewManager.loadUrl(url);
        }
        return tab;
    }

    public void closeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        Tab tab = tabs.get(index);
        if (tab.webViewManager != null) {
            container.removeView(tab.webViewManager.getWebView());
            tab.webViewManager.onDestroy();
        }
        tabs.remove(index);
        if (tabs.isEmpty()) {
            activeIndex = -1;
        } else {
            if (index < activeIndex) {
                activeIndex--;
            } else if (index == activeIndex) {
                if (activeIndex >= tabs.size()) {
                    activeIndex = tabs.size() - 1;
                }
            }
            if (activeIndex >= 0 && activeIndex < tabs.size()) {
                showTab(activeIndex);
            }
        }
    }

    public void switchToTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        if (index == activeIndex) return;
        hideTab(activeIndex);
        activeIndex = index;
        showTab(activeIndex);
    }

    private void showTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        Tab tab = tabs.get(index);
        if (tab.webViewManager != null) {
            WebView webView = tab.webViewManager.getWebView();
            if (webView != null) {
                ViewGroup parent = (ViewGroup) webView.getParent();
                if (parent != null) parent.removeView(webView);
                container.addView(webView, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                webView.requestFocus();
                tab.webViewManager.onResume();
            }
        }
    }

    private void hideTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        Tab tab = tabs.get(index);
        if (tab.webViewManager != null) {
            tab.webViewManager.onPause();
            WebView webView = tab.webViewManager.getWebView();
            if (webView != null) {
                container.removeView(webView);
            }
        }
    }

    public void closeAllTabs() {
        for (Tab tab : tabs) {
            if (tab.webViewManager != null) {
                tab.webViewManager.onDestroy();
            }
        }
        tabs.clear();
        container.removeAllViews();
        activeIndex = -1;
    }

    public Tab getActiveTab() {
        if (activeIndex >= 0 && activeIndex < tabs.size()) {
            return tabs.get(activeIndex);
        }
        return null;
    }

    public int getActiveTabIndex() {
        return activeIndex;
    }

    public int getTabCount() {
        return tabs.size();
    }

    public List<Tab> getAllTabs() {
        return new ArrayList<>(tabs);
    }

    public Tab getTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            return tabs.get(index);
        }
        return null;
    }
}
