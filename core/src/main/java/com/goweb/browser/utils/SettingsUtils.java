package com.goweb.browser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.goweb.browser.App;

public class SettingsUtils {

    private static final String KEY_SEARCH_ENGINE = "search_engine";
    private static final String KEY_HOMEPAGE = "homepage";
    private static final String KEY_UA = "user_agent";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_PRIVACY_MODE = "privacy_mode";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_BLOCK_IMAGES = "block_images";
    private static final String KEY_SAVE_PASSWORDS = "save_passwords";
    private static final String KEY_ENABLE_JS = "enable_js";
    private static final String KEY_ENABLE_COOKIES = "enable_cookies";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HOMEPAGE_BG = "homepage_bg";
    private static final String KEY_HOMEPAGE_ICON = "homepage_icon";
    private static final String KEY_CUSTOM_FONT = "custom_font";
    private static final String KEY_NIGHT_MODE_CHANGED = "night_mode_changed";

    public static final int ENGINE_BAIDU = 0;
    public static final int ENGINE_GOOGLE = 1;
    public static final int ENGINE_BING = 2;
    public static final int ENGINE_DUCKDUCKGO = 3;
    public static final int ENGINE_SOGOU = 4;

    public static final int UA_MOBILE = 0;
    public static final int UA_PC = 1;
    public static final int UA_CUSTOM = 2;

    public static final int LANG_AUTO = 0;
    public static final int LANG_ENGLISH = 1;
    public static final int LANG_CHINESE = 2;

    private static final String[] SEARCH_URLS = {
        "https://www.baidu.com/s?wd=",
        "https://www.google.com/search?q=",
        "https://www.bing.com/search?q=",
        "https://duckduckgo.com/?q=",
        "https://www.sogou.com/web?query="
    };

    private static final String[] SEARCH_NAMES = {
        "Baidu",
        "Google",
        "Bing",
        "DuckDuckGo",
        "Sogou"
    };

    private static final String[] SEARCH_HOMEPAGES = {
        "https://www.baidu.com",
        "https://www.google.com",
        "https://www.bing.com",
        "https://duckduckgo.com",
        "https://www.sogou.com"
    };

    private static final String DEFAULT_HOMEPAGE = "https://www.baidu.com";

    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    public static int getSearchEngine() {
        return getPrefs().getInt(KEY_SEARCH_ENGINE, ENGINE_BAIDU);
    }

    public static void setSearchEngine(int engine) {
        getPrefs().edit().putInt(KEY_SEARCH_ENGINE, engine).apply();
        setHomepage(SEARCH_HOMEPAGES[engine]);
    }

    public static String getSearchUrl() {
        return SEARCH_URLS[getSearchEngine()];
    }

    public static String[] getSearchEngineNames() {
        return SEARCH_NAMES;
    }

    public static String[] getSearchEngineUrls() {
        return SEARCH_URLS;
    }

    public static String getHomepage() {
        return getPrefs().getString(KEY_HOMEPAGE, DEFAULT_HOMEPAGE);
    }

    public static void setHomepage(String url) {
        getPrefs().edit().putString(KEY_HOMEPAGE, url).apply();
    }

    public static int getUaMode() {
        return getPrefs().getInt(KEY_UA, UA_MOBILE);
    }

    public static void setUaMode(int mode) {
        getPrefs().edit().putInt(KEY_UA, mode).apply();
    }

    public static String getCustomUa() {
        return getPrefs().getString("custom_ua", "");
    }

    public static void setCustomUa(String ua) {
        getPrefs().edit().putString("custom_ua", ua).apply();
    }

    public static boolean isNightMode() {
        return getPrefs().getBoolean(KEY_NIGHT_MODE, false);
    }

    public static void setNightMode(boolean enabled) {
        boolean old = isNightMode();
        getPrefs().edit().putBoolean(KEY_NIGHT_MODE, enabled).apply();
        getPrefs().edit().putBoolean(KEY_NIGHT_MODE_CHANGED, old != enabled).apply();
    }

    public static boolean isNightModeChanged() {
        boolean changed = getPrefs().getBoolean(KEY_NIGHT_MODE_CHANGED, false);
        getPrefs().edit().putBoolean(KEY_NIGHT_MODE_CHANGED, false).apply();
        return changed;
    }

    public static boolean isPrivacyMode() {
        return getPrefs().getBoolean(KEY_PRIVACY_MODE, false);
    }

    public static void setPrivacyMode(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply();
    }

    public static int getFontSize() {
        return getPrefs().getInt(KEY_FONT_SIZE, 100);
    }

    public static void setFontSize(int size) {
        getPrefs().edit().putInt(KEY_FONT_SIZE, size).apply();
    }

    public static boolean isBlockImages() {
        return getPrefs().getBoolean(KEY_BLOCK_IMAGES, false);
    }

    public static void setBlockImages(boolean block) {
        getPrefs().edit().putBoolean(KEY_BLOCK_IMAGES, block).apply();
    }

    public static boolean isSavePasswords() {
        return getPrefs().getBoolean(KEY_SAVE_PASSWORDS, false);
    }

    public static void setSavePasswords(boolean save) {
        getPrefs().edit().putBoolean(KEY_SAVE_PASSWORDS, save).apply();
    }

    public static boolean isEnableJs() {
        return getPrefs().getBoolean(KEY_ENABLE_JS, true);
    }

    public static void setEnableJs(boolean enable) {
        getPrefs().edit().putBoolean(KEY_ENABLE_JS, enable).apply();
    }

    public static boolean isEnableCookies() {
        return getPrefs().getBoolean(KEY_ENABLE_COOKIES, true);
    }

    public static void setEnableCookies(boolean enable) {
        getPrefs().edit().putBoolean(KEY_ENABLE_COOKIES, enable).apply();
    }

    public static int getLanguage() {
        return getPrefs().getInt(KEY_LANGUAGE, LANG_AUTO);
    }

    public static int getLanguage(Context context) {
        if (context == null) return getLanguage();
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_LANGUAGE, LANG_AUTO);
    }

    public static void setLanguage(int language) {
        getPrefs().edit().putInt(KEY_LANGUAGE, language).apply();
    }

    public static String getHomepageBg() {
        return getPrefs().getString(KEY_HOMEPAGE_BG, "");
    }

    public static void setHomepageBg(String path) {
        getPrefs().edit().putString(KEY_HOMEPAGE_BG, path).apply();
    }

    public static String getHomepageIcon() {
        return getPrefs().getString(KEY_HOMEPAGE_ICON, "");
    }

    public static void setHomepageIcon(String path) {
        getPrefs().edit().putString(KEY_HOMEPAGE_ICON, path).apply();
    }

    public static String getCustomFont() {
        return getPrefs().getString(KEY_CUSTOM_FONT, "");
    }

    public static void setCustomFont(String path) {
        getPrefs().edit().putString(KEY_CUSTOM_FONT, path).apply();
    }

    public static void clearCache() {
        App.getContext().deleteDatabase("webview.db");
        App.getContext().deleteDatabase("webviewCache.db");
    }

    public static void clearAllData() {
        getPrefs().edit().clear().apply();
        BookmarkUtils.clearAll();
        HistoryUtils.clearAll();
    }
}
