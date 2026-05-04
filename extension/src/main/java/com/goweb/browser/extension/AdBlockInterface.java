package com.goweb.browser.extension;

import java.util.List;

public interface AdBlockInterface {

    interface AdRule {
        String getPattern();
        boolean isWhitelist();
        String getType();
    }

    void loadRules(List<AdRule> rules);

    void loadRulesFromAsset(String assetPath);

    boolean shouldBlock(String url, String pageHost);

    int getBlockedCount();

    void resetCount();

    void setEnabled(boolean enabled);

    boolean isEnabled();
}
