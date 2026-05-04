package com.goweb.browser.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.URLUtil;

import com.goweb.browser.App;

import java.io.File;
import java.net.URLDecoder;

public class DownloadUtils {

    public static void download(Context context, String url, String contentDisposition, String mimeType) {
        download(context, url, contentDisposition, mimeType, "");
    }

    public static void download(Context context, String url, String contentDisposition, String mimeType, String cookies) {
        try {
            if (url == null || url.isEmpty()) return;
            if (url.startsWith("blob:") || url.startsWith("javascript:")) return;
            if (url.startsWith("data:")) return;

            Uri uri = Uri.parse(url);
            String fileName = getFileName(url, contentDisposition, mimeType);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            request.setDescription("GoWeb-Download");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }

            if (mimeType != null && !mimeType.isEmpty() && !mimeType.equals("*/*")) {
                request.setMimeType(mimeType);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            }

            request.addRequestHeader("User-Agent", System.getProperty("http.agent"));
            if (cookies != null && !cookies.isEmpty()) {
                request.addRequestHeader("Cookie", cookies);
            }
            request.addRequestHeader("Referer", url);

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileName(String url, String contentDisposition, String mimeType) {
        String fileName = "";

        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            int index = contentDisposition.indexOf("filename=");
            if (index >= 0) {
                fileName = contentDisposition.substring(index + 9);
                fileName = fileName.replace("\"", "").replace(";", "").trim();
            }
            if (fileName.isEmpty()) {
                int utfIndex = contentDisposition.indexOf("filename*=UTF-8''");
                if (utfIndex >= 0) {
                    fileName = contentDisposition.substring(utfIndex + 17);
                    fileName = fileName.replace("\"", "").replace(";", "").trim();
                    try {
                        fileName = URLDecoder.decode(fileName, "UTF-8");
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (fileName.isEmpty()) {
            fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        }

        if (fileName.isEmpty() || fileName.equals("downloadfile")) {
            int slashIndex = url.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex < url.length() - 1) {
                String query = url.substring(slashIndex + 1);
                int qIndex = query.indexOf('?');
                if (qIndex > 0) query = query.substring(0, qIndex);
                if (!query.isEmpty()) fileName = query;
            }
        }

        if (fileName.isEmpty()) {
            fileName = "download_" + System.currentTimeMillis();
        }

        return fileName;
    }

    public static String getDownloadStatusText(Context context, int status) {
        switch (status) {
            case DownloadManager.STATUS_PENDING:
                return "Pending";
            case DownloadManager.STATUS_RUNNING:
                return "Downloading";
            case DownloadManager.STATUS_PAUSED:
                return "Paused";
            case DownloadManager.STATUS_SUCCESSFUL:
                return "Completed";
            case DownloadManager.STATUS_FAILED:
                return "Failed";
            default:
                return "Unknown";
        }
    }

    public static int getDownloadStatusColor(int status) {
        switch (status) {
            case DownloadManager.STATUS_PENDING:
                return 0xFF9E9E9E;
            case DownloadManager.STATUS_RUNNING:
                return 0xFF2d6a4f;
            case DownloadManager.STATUS_PAUSED:
                return 0xFFFF9800;
            case DownloadManager.STATUS_SUCCESSFUL:
                return 0xFF4CAF50;
            case DownloadManager.STATUS_FAILED:
                return 0xFFF44336;
            default:
                return 0xFF9E9E9E;
        }
    }
}
