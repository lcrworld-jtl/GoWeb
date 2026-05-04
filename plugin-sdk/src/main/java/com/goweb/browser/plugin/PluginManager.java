package com.goweb.browser.plugin;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginManager {

    private static final String PREFS_NAME = "plugin_prefs";
    private static final String KEY_ENABLED_PLUGINS = "enabled_plugins";

    private static PluginManager instance;
    private final Context context;
    private final List<PluginInterface> plugins;
    private final SharedPreferences prefs;

    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        this.plugins = new ArrayList<>();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }

    public void registerPlugin(PluginInterface plugin) {
        if (plugin == null) {
            return;
        }
        for (PluginInterface p : plugins) {
            if (p.getPluginInfo().getId().equals(plugin.getPluginInfo().getId())) {
                return;
            }
        }
        plugins.add(plugin);
        if (isPluginEnabled(plugin.getPluginInfo().getId())) {
            plugin.onCreate(context, plugin.getPluginInfo());
            plugin.onEnabled();
        }
    }

    public void unregisterPlugin(String pluginId) {
        for (int i = plugins.size() - 1; i >= 0; i--) {
            PluginInterface plugin = plugins.get(i);
            if (plugin.getPluginInfo().getId().equals(pluginId)) {
                plugin.onDisabled();
                plugin.onDestroy();
                plugins.remove(i);
                break;
            }
        }
    }

    public void enablePlugin(String pluginId) {
        PluginInterface plugin = findPlugin(pluginId);
        if (plugin != null) {
            plugin.onEnabled();
            saveEnabledState(pluginId, true);
        }
    }

    public void disablePlugin(String pluginId) {
        PluginInterface plugin = findPlugin(pluginId);
        if (plugin != null) {
            plugin.onDisabled();
            saveEnabledState(pluginId, false);
        }
    }

    public boolean isPluginEnabled(String pluginId) {
        return prefs.getBoolean("plugin_enabled_" + pluginId, false);
    }

    public PluginInterface findPlugin(String pluginId) {
        for (PluginInterface plugin : plugins) {
            if (plugin.getPluginInfo().getId().equals(pluginId)) {
                return plugin;
            }
        }
        return null;
    }

    public List<PluginInterface> getAllPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public List<PluginInterface> getEnabledPlugins() {
        List<PluginInterface> enabled = new ArrayList<>();
        for (PluginInterface plugin : plugins) {
            if (isPluginEnabled(plugin.getPluginInfo().getId())) {
                enabled.add(plugin);
            }
        }
        return enabled;
    }

    private void saveEnabledState(String pluginId, boolean enabled) {
        prefs.edit().putBoolean("plugin_enabled_" + pluginId, enabled).apply();
    }

    public void destroyAll() {
        for (PluginInterface plugin : plugins) {
            plugin.onDisabled();
            plugin.onDestroy();
        }
        plugins.clear();
    }
}
