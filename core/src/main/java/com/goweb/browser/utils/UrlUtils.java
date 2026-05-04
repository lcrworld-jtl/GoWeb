package com.goweb.browser.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,})(/\\S*)?$"
    );

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(https?://)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?(/\\S*)?$"
    );

    private static final Pattern CHINESE_PATTERN = Pattern.compile(
            "[\\u4e00-\\u9fa5]"
    );

    public static boolean isUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        input = input.trim();
        if (URL_PATTERN.matcher(input).matches()) {
            return true;
        }
        if (IP_PATTERN.matcher(input).matches()) {
            return true;
        }
        if (input.startsWith("http://") || input.startsWith("https://") ||
                input.startsWith("file:///")) {
            return true;
        }
        return false;
    }

    public static boolean isSearchQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        if (CHINESE_PATTERN.matcher(input).find()) {
            return true;
        }
        if (input.contains(" ") && !input.startsWith("http")) {
            return true;
        }
        return !isUrl(input);
    }

    public static String toSearchUrl(String query) {
        return SettingsUtils.getSearchUrl() + android.net.Uri.encode(query);
    }

    public static String normalizeUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        input = input.trim();
        if (isUrl(input)) {
            if (!input.startsWith("http://") && !input.startsWith("https://") &&
                    !input.startsWith("file:///")) {
                input = "https://" + input;
            }
            return input;
        }
        return toSearchUrl(input);
    }

    public static String getHost(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    public static String getDomain(String url) {
        String host = getHost(url);
        if (host == null) {
            return url;
        }
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }
        return host;
    }
}
