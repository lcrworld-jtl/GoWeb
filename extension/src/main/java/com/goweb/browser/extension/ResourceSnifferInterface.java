package com.goweb.browser.extension;

import java.util.List;

public interface ResourceSnifferInterface {

    interface ResourceItem {
        String getUrl();
        String getType();
        String getFileName();
        long getSize();
    }

    interface SniffCallback {
        void onResourceFound(ResourceItem resource);
        void onSniffComplete(List<ResourceItem> resources);
        void onSniffError(String errorMessage);
    }

    void startSniffing(String pageUrl, SniffCallback callback);

    void stopSniffing();

    List<ResourceItem> getSniffedResources();

    void downloadResource(ResourceItem resource, String savePath);

    void setEnabled(boolean enabled);

    boolean isEnabled();
}
