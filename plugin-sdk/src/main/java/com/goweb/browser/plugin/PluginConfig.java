package com.goweb.browser.plugin;

public class PluginConfig {

    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String iconPath;
    private String minAppVersion;
    private boolean enabled;

    public PluginConfig() {
        this.version = "1.0.0";
        this.minAppVersion = "1.0.0";
        this.enabled = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public String getMinAppVersion() { return minAppVersion; }
    public void setMinAppVersion(String minAppVersion) { this.minAppVersion = minAppVersion; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
