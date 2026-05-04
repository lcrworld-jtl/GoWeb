package com.goweb.browser.utils;

import android.content.Context;
import android.webkit.WebViewClient;

import com.goweb.browser.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "tel:(\\+?\\d[\\d\\-]{7,})"
    );

    public static String extractTitleFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (host == null) return url;
            if (host.startsWith("www.")) host = host.substring(4);
            StringBuilder title = new StringBuilder();
            for (String part : host.split("\\.")) {
                if (title.length() > 0) title.append(" ");
                if (part.length() > 0) {
                    title.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) title.append(part.substring(1));
                }
            }
            return title.toString();
        } catch (Exception e) {
            return url;
        }
    }

    public static String buildErrorPage(Context context, int errorCode, String description, String failingUrl) {
        String errorTitle = context.getString(R.string.page_load_error);
        String errorDesc = getErrorDescription(context, errorCode, description);
        String retry = context.getString(R.string.retry);
        String tipsTitle = context.getString(R.string.error_tips_title);
        String tip1 = context.getString(R.string.error_tip_1);
        String tip2 = context.getString(R.string.error_tip_2);
        String tip3 = context.getString(R.string.error_tip_3);

        String errorIcon = getErrorIcon(errorCode);

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                "<style>" +
                "*{margin:0;padding:0;box-sizing:border-box;}" +
                "body{font-family:-apple-system,sans-serif;background:#f0f7f4;" +
                "display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px;}" +
                ".container{background:#fff;border-radius:20px;padding:40px 30px;" +
                "text-align:center;max-width:360px;width:100%;" +
                "box-shadow:0 8px 32px rgba(45,106,79,0.08);}" +
                ".icon{font-size:56px;margin-bottom:20px;animation:bounce 2s infinite;}" +
                "@keyframes bounce{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}" +
                "h2{color:#2d6a4f;margin:0 0 12px 0;font-size:20px;font-weight:700;}" +
                ".desc{color:#666;margin:0 0 20px 0;font-size:14px;line-height:1.6;}" +
                ".url{color:#999;margin:0 0 24px 0;font-size:12px;word-break:break-all;" +
                "background:#f5f5f5;padding:8px 12px;border-radius:8px;}" +
                ".btn{display:inline-block;padding:12px 36px;background:#2d6a4f;" +
                "color:#fff;text-decoration:none;border-radius:24px;font-size:14px;" +
                "font-weight:600;transition:all 0.3s;box-shadow:0 4px 12px rgba(45,106,79,0.2);}" +
                ".btn:hover{background:#52b788;transform:translateY(-1px);" +
                "box-shadow:0 6px 16px rgba(45,106,79,0.3);}" +
                ".btn:active{transform:translateY(0);}" +
                ".tips{margin-top:24px;padding-top:20px;border-top:1px solid #e0f0e9;text-align:left;}" +
                ".tips-title{color:#2d6a4f;font-size:13px;margin-bottom:10px;font-weight:700;}" +
                ".tips-item{color:#888;font-size:12px;margin:6px 0;padding-left:16px;position:relative;}" +
                ".tips-item::before{content:'';position:absolute;left:0;top:7px;" +
                "width:6px;height:6px;background:#52b788;border-radius:50%;}" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='icon'>" + errorIcon + "</div>" +
                "<h2>" + errorTitle + "</h2>" +
                "<p class='desc'>" + errorDesc + "</p>" +
                "<p class='url'>" + escapeHtml(failingUrl) + "</p>" +
                "<a href='" + failingUrl + "' class='btn'>" + retry + "</a>" +
                "<div class='tips'>" +
                "<div class='tips-title'>" + tipsTitle + "</div>" +
                "<div class='tips-item'>" + tip1 + "</div>" +
                "<div class='tips-item'>" + tip2 + "</div>" +
                "<div class='tips-item'>" + tip3 + "</div>" +
                "</div></div></body></html>";
    }

    private static String getErrorIcon(int errorCode) {
        switch (errorCode) {
            case WebViewClient.ERROR_HOST_LOOKUP:
                return "&#127760;";
            case WebViewClient.ERROR_CONNECT:
                return "&#128276;";
            case WebViewClient.ERROR_TIMEOUT:
                return "&#9203;";
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                return "&#128274;";
            default:
                return "&#128683;";
        }
    }

    private static String getErrorDescription(Context context, int errorCode, String defaultDesc) {
        switch (errorCode) {
            case WebViewClient.ERROR_HOST_LOOKUP:
                return context.getString(R.string.error_host_lookup);
            case WebViewClient.ERROR_CONNECT:
                return context.getString(R.string.error_connect);
            case WebViewClient.ERROR_TIMEOUT:
                return context.getString(R.string.error_timeout);
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                return context.getString(R.string.error_ssl);
            default:
                return defaultDesc != null && !defaultDesc.isEmpty() ? defaultDesc : context.getString(R.string.error_unknown);
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String extractUrlsFromText(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder urls = new StringBuilder();
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            if (urls.length() > 0) urls.append("\n");
            urls.append(matcher.group());
        }
        return urls.toString();
    }

    public static boolean containsUrl(String text) {
        if (text == null || text.isEmpty()) return false;
        return URL_PATTERN.matcher(text).find();
    }

    public static boolean containsEmail(String text) {
        if (text == null || text.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(text).find();
    }

    public static String buildShareText(String title, String url) {
        if (title == null || title.isEmpty()) return url != null ? url : "";
        if (url == null || url.isEmpty()) return title;
        return title + "\n" + url;
    }

    public static String buildTranslateUrl(String originalUrl, String targetLang) {
        if (originalUrl == null || originalUrl.isEmpty()) return "";
        return "https://translate.google.com/translate?hl=" + targetLang +
                "&sl=auto&tl=" + targetLang + "&u=" + android.net.Uri.encode(originalUrl);
    }

    public static String buildCacheBusterUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "_t=" + System.currentTimeMillis();
    }

    public static boolean isDataUrl(String url) {
        return url != null && url.startsWith("data:");
    }

    public static boolean isBlobUrl(String url) {
        return url != null && url.startsWith("blob:");
    }

    public static boolean isJavascriptUrl(String url) {
        return url != null && url.startsWith("javascript:");
    }

    public static boolean isFileUrl(String url) {
        return url != null && url.startsWith("file:///");
    }

    public static boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static String getUserAgentString(int mode) {
        switch (mode) {
            case SettingsUtils.UA_PC:
                return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            case SettingsUtils.UA_CUSTOM:
                return SettingsUtils.getCustomUa();
            default:
                return "";
        }
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 0) return formatTimestamp(timestamp);
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " min ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        if (diff < 604800000) return (diff / 86400000) + " days ago";
        return formatTimestamp(timestamp);
    }

    public static long getDirSize(File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += getDirSize(f);
                else size += f.length();
            }
        }
        return size;
    }

    public static boolean deleteDirContents(File dir) {
        if (dir == null || !dir.exists()) return false;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirContents(f);
                f.delete();
            }
        }
        return true;
    }

    public static boolean writeFile(File file, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(content);
            osw.flush();
            osw.close();
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            isr.close();
            fis.close();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
