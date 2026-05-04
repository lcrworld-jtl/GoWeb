package com.goweb.browser.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.goweb.browser.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SystemUtils {

    public static String getAppVersionName() {
        try {
            PackageInfo info = App.getContext().getPackageManager()
                    .getPackageInfo(App.getContext().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    public static int getAppVersionCode() {
        try {
            PackageInfo info = App.getContext().getPackageManager()
                    .getPackageInfo(App.getContext().getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= 28) {
                return (int) info.getLongVersionCode();
            }
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getPackageName() {
        return App.getContext().getPackageName();
    }

    public static String getAppName() {
        try {
            ApplicationInfo info = App.getContext().getApplicationInfo();
            return App.getContext().getPackageManager().getApplicationLabel(info).toString();
        } catch (Exception e) {
            return "GoWeb";
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    public static String getDeviceBrand() {
        return capitalize(Build.BRAND);
    }

    public static String getDeviceManufacturer() {
        return capitalize(Build.MANUFACTURER);
    }

    public static String getDeviceBoard() {
        return Build.BOARD;
    }

    public static String getDeviceHardware() {
        return Build.HARDWARE;
    }

    public static String getDeviceFingerprint() {
        return Build.FINGERPRINT;
    }

    public static String getBuildId() {
        return Build.DISPLAY;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toUpperCase(c);
                capitalizeNext = false;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static long getTotalInternalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long totalBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

    public static long getAvailableInternalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    public static long getTotalExternalStorage() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long totalBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalStorage() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long availableBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    public static String formatStorageSize(long bytes) {
        return Formatter.formatFileSize(App.getContext(), bytes);
    }

    public static long getTotalMemory() {
        try {
            File meminfo = new File("/proc/meminfo");
            if (meminfo.exists()) {
                FileInputStream fis = new FileInputStream(meminfo);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                br.close();
                if (line != null && line.startsWith("MemTotal:")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]) * 1024;
                    }
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static long getAvailableMemory() {
        try {
            File meminfo = new File("/proc/meminfo");
            if (meminfo.exists()) {
                FileInputStream fis = new FileInputStream(meminfo);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                br.close();
                if (line != null && line.startsWith("MemAvailable:")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]) * 1024;
                    }
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static long getAppCacheSize() {
        File cacheDir = App.getContext().getCacheDir();
        return DataUtils.getDirectorySize(cacheDir);
    }

    public static long getAppDataSize() {
        File dataDir = App.getContext().getFilesDir();
        return DataUtils.getDirectorySize(dataDir);
    }

    public static boolean clearAppCache() {
        File cacheDir = App.getContext().getCacheDir();
        return DataUtils.deleteDirectory(cacheDir);
    }

    public static String getCacheSizeFormatted() {
        return DataUtils.formatFileSize(getAppCacheSize());
    }

    public static String getDataSizeFormatted() {
        return DataUtils.formatFileSize(getAppDataSize());
    }

    public static String getStorageInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Internal Storage:\n");
        sb.append("  Total: ").append(formatStorageSize(getTotalInternalStorage())).append("\n");
        sb.append("  Available: ").append(formatStorageSize(getAvailableInternalStorage())).append("\n");
        sb.append("\nExternal Storage:\n");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sb.append("  Total: ").append(formatStorageSize(getTotalExternalStorage())).append("\n");
            sb.append("  Available: ").append(formatStorageSize(getAvailableExternalStorage()));
        } else {
            sb.append("  Not Available");
        }
        return sb.toString();
    }

    public static String getDeviceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Device: ").append(getDeviceModel()).append("\n");
        sb.append("Brand: ").append(getDeviceBrand()).append("\n");
        sb.append("Manufacturer: ").append(getDeviceManufacturer()).append("\n");
        sb.append("Android: ").append(getAndroidVersion()).append("\n");
        sb.append("Board: ").append(getDeviceBoard()).append("\n");
        sb.append("Hardware: ").append(getDeviceHardware());
        return sb.toString();
    }

    public static String getAppInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("App: ").append(getAppName()).append("\n");
        sb.append("Version: ").append(getAppVersionName()).append(" (").append(getAppVersionCode()).append(")\n");
        sb.append("Package: ").append(getPackageName()).append("\n");
        sb.append("Cache: ").append(getCacheSizeFormatted()).append("\n");
        sb.append("Data: ").append(getDataSizeFormatted());
        return sb.toString();
    }

    public static String getFullSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Device Info ===\n");
        sb.append(getDeviceInfo()).append("\n\n");
        sb.append("=== App Info ===\n");
        sb.append(getAppInfo()).append("\n\n");
        sb.append("=== Storage ===\n");
        sb.append(getStorageInfo()).append("\n\n");
        sb.append("=== Network ===\n");
        sb.append(NetworkUtils.getNetworkInfoString());
        return sb.toString();
    }

    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static File getDownloadDirectory() {
        if (isExternalStorageAvailable()) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
        return App.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getSavedPagesDirectory() {
        File dir = new File(App.getContext().getFilesDir(), "saved_pages");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getScreenshotDirectory() {
        File dir = new File(App.getContext().getFilesDir(), "screenshots");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isAtLeast(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isKitKatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isNougatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isPieOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static boolean isAndroid10OrAbove() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static boolean isAndroid11OrAbove() {
        return Build.VERSION.SDK_INT >= 30;
    }

    public static boolean isAndroid12OrAbove() {
        return Build.VERSION.SDK_INT >= 31;
    }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        return sdf.format(new Date());
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getUptime() {
        long uptime = System.currentTimeMillis() - getStartTime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    private static long startTime = System.currentTimeMillis();

    private static long getStartTime() {
        return startTime;
    }

    public static List<ApplicationInfo> getInstalledApps() {
        PackageManager pm = App.getContext().getPackageManager();
        return pm.getInstalledApplications(0);
    }

    public static boolean isAppInstalled(String packageName) {
        try {
            App.getContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean launchApp(String packageName) {
        try {
            Intent intent = App.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getContext().startActivity(intent);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static void openAppInMarket(Context context, String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("market://details?id=" + packageName));
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                context.startActivity(intent);
            } catch (Exception e2) {
            }
        }
    }
}
