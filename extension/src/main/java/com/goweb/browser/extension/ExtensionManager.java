package com.goweb.browser.extension;

public interface ExtensionManager {

    interface ExtensionInfo {
        String getId();
        String getName();
        String getVersion();
        String getDescription();
        String getAuthor();
        boolean isEnabled();
    }

    void initialize();

    AdBlockInterface getAdBlock();

    TranslateInterface getTranslator();

    NightModeInterface getNightMode();

    ResourceSnifferInterface getResourceSniffer();

    void registerExtension(ExtensionInfo extension);

    void unregisterExtension(String extensionId);

    java.util.List<ExtensionInfo> getInstalledExtensions();

    boolean isExtensionAvailable(String extensionId);
}
