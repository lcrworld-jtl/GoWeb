package com.goweb.browser.plugin;

import android.content.Context;

public interface PluginInterface {

    String PLUGIN_ACTION = "com.goweb.browser.plugin.PLUGIN";

    interface PluginInfo {
        String getId();
        String getName();
        String getVersion();
        String getDescription();
        String getAuthor();
        String getIconPath();
        String getMinAppVersion();
    }

    void onCreate(Context context, PluginInfo info);

    void onDestroy();

    void onEnabled();

    void onDisabled();

    PluginInfo getPluginInfo();

    boolean isRunning();

    String[] getRequiredPermissions();
}
