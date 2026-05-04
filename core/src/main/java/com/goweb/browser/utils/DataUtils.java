package com.goweb.browser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.goweb.browser.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataUtils {

    private static final String PREFS_NAME = "goweb_data";
    private static final String KEY_EXTRA_DATA = "extra_data";

    public static SharedPreferences getDataPrefs() {
        return App.getContext().getSharedPreferences(PREFS_NAME, 0);
    }

    public static void putString(String key, String value) {
        getDataPrefs().edit().putString(key, value).apply();
    }

    public static String getString(String key, String defaultValue) {
        return getDataPrefs().getString(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        getDataPrefs().edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int defaultValue) {
        return getDataPrefs().getInt(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        getDataPrefs().edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long defaultValue) {
        return getDataPrefs().getLong(key, defaultValue);
    }

    public static void putBoolean(String key, boolean value) {
        getDataPrefs().edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getDataPrefs().getBoolean(key, defaultValue);
    }

    public static void putFloat(String key, float value) {
        getDataPrefs().edit().putFloat(key, value).apply();
    }

    public static float getFloat(String key, float defaultValue) {
        return getDataPrefs().getFloat(key, defaultValue);
    }

    public static void remove(String key) {
        getDataPrefs().edit().remove(key).apply();
    }

    public static boolean contains(String key) {
        return getDataPrefs().contains(key);
    }

    public static void putJSONObject(String key, JSONObject object) {
        if (object == null) {
            remove(key);
            return;
        }
        putString(key, object.toString());
    }

    public static JSONObject getJSONObject(String key) {
        String json = getString(key, "");
        if (json.isEmpty()) return new JSONObject();
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public static void putJSONArray(String key, JSONArray array) {
        if (array == null) {
            remove(key);
            return;
        }
        putString(key, array.toString());
    }

    public static JSONArray getJSONArray(String key) {
        String json = getString(key, "");
        if (json.isEmpty()) return new JSONArray();
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static void putStringList(String key, List<String> list) {
        if (list == null) {
            remove(key);
            return;
        }
        JSONArray array = new JSONArray();
        for (String item : list) {
            array.put(item);
        }
        putString(key, array.toString());
    }

    public static List<String> getStringList(String key) {
        List<String> list = new ArrayList<>();
        String json = getString(key, "");
        if (json.isEmpty()) return list;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.optString(i, ""));
            }
        } catch (JSONException e) {
        }
        return list;
    }

    public static void putStringMap(String key, Map<String, String> map) {
        if (map == null) {
            remove(key);
            return;
        }
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
        }
        putString(key, obj.toString());
    }

    public static Map<String, String> getStringMap(String key) {
        Map<String, String> map = new LinkedHashMap<>();
        String json = getString(key, "");
        if (json.isEmpty()) return map;
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray keys = obj.names();
            if (keys != null) {
                for (int i = 0; i < keys.length(); i++) {
                    String k = keys.getString(i);
                    map.put(k, obj.optString(k, ""));
                }
            }
        } catch (JSONException e) {
        }
        return map;
    }

    public static String serializeToString(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static Object deserializeFromString(String str) throws IOException, ClassNotFoundException {
        byte[] data = Base64.decode(str, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(baos);
        gzip.write(data);
        gzip.close();
        return baos.toByteArray();
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        GZIPInputStream gzip = new GZIPInputStream(bais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = gzip.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        gzip.close();
        baos.close();
        return baos.toByteArray();
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String base64Encode(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String base64Decode(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }

    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }

    public static String formatNumber(double number) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        if (nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).applyPattern("#,##0.##");
        }
        return nf.format(number);
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public static String formatSpeed(long bytesPerSecond) {
        return formatFileSize(bytesPerSecond) + "/s";
    }

    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            return String.format(Locale.US, "%dh %dm", hours, minutes % 60);
        }
        if (minutes > 0) {
            return String.format(Locale.US, "%dm %ds", minutes, seconds % 60);
        }
        return String.format(Locale.US, "%ds", seconds);
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 0) return formatDateTime(timestamp);
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " min ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        if (diff < 604800000) return (diff / 86400000) + " days ago";
        if (diff < 2592000000L) return (diff / 604800000) + " weeks ago";
        return formatDate(timestamp);
    }

    public static boolean isSameDay(long timestamp1, long timestamp2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date(timestamp1)).equals(sdf.format(new Date(timestamp2)));
    }

    public static boolean isToday(long timestamp) {
        return isSameDay(timestamp, System.currentTimeMillis());
    }

    public static boolean isYesterday(long timestamp) {
        return isSameDay(timestamp, System.currentTimeMillis() - 86400000);
    }

    public static String getDomainFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            URL u = new URL(url);
            String host = u.getHost();
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host != null ? host : "";
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public static String getBaseUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            URL u = new URL(url);
            return u.getProtocol() + "://" + u.getAuthority();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) return params;
        if (query.startsWith("?")) query = query.substring(1);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = urlDecode(pair.substring(0, idx));
                String value = urlDecode(pair.substring(idx + 1));
                params.put(key, value);
            } else {
                params.put(urlDecode(pair), "");
            }
        }
        return params;
    }

    public static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    public static List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        if (text == null || text.isEmpty()) return urls;
        Pattern pattern = Pattern.compile("https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*?");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    public static String defaultIfEmpty(String text, String defaultValue) {
        return isEmpty(text) ? defaultValue : text;
    }

    public static String join(List<String> list, String separator) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    public static List<String> split(String text, String delimiter) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;
        String[] parts = text.split(delimiter);
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    public static String reverse(String text) {
        if (text == null || text.isEmpty()) return text;
        return new StringBuilder(text).reverse().toString();
    }

    public static int countOccurrences(String text, String substring) {
        if (text == null || substring == null || substring.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(substring, idx)) != -1) {
            count++;
            idx += substring.length();
        }
        return count;
    }

    public static String repeat(String text, int times) {
        if (text == null || times <= 0) return "";
        StringBuilder sb = new StringBuilder(text.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    public static boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isAlphanumeric(String text) {
        if (text == null || text.isEmpty()) return false;
        for (char c : text.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        return pattern.matcher(email).matches();
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        Pattern pattern = Pattern.compile("^https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*");
        return pattern.matcher(url).matches();
    }

    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        Pattern pattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
        if (!pattern.matcher(ip).matches()) return false;
        String[] parts = ip.split("\\.");
        for (String part : parts) {
            int num = Integer.parseInt(part);
            if (num < 0 || num > 255) return false;
        }
        return true;
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String unescapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    public static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static boolean copyFile(File src, File dst) {
        try {
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dst);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean moveFile(File src, File dst) {
        if (copyFile(src, dst)) {
            return src.delete();
        }
        return false;
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) return false;
        return file.delete();
    }

    public static boolean deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return false;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        return dir.delete();
    }

    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += getDirectorySize(f);
                else size += f.length();
            }
        }
        return size;
    }

    public static int getFileCount(File dir) {
        int count = 0;
        if (dir == null || !dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) count += getFileCount(f);
                else count++;
            }
        }
        return count;
    }

    public static String readFileAsString(File file) {
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
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean writeStringToFile(File file, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(content);
            osw.flush();
            osw.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex >= fileName.length() - 1) return "";
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public static String getNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) return fileName;
        return fileName.substring(0, dotIndex);
    }

    public static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) return "unnamed";
        return name.replaceAll("[^a-zA-Z0-9.\\-_\\u4e00-\\u9fa5]", "_");
    }

    public static String generateUniqueFileName(String directory, String baseName, String extension) {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();
        String fileName = baseName + "." + extension;
        File file = new File(dir, fileName);
        if (!file.exists()) return fileName;
        int counter = 1;
        while (file.exists()) {
            fileName = baseName + "_" + counter + "." + extension;
            file = new File(dir, fileName);
            counter++;
        }
        return fileName;
    }
}
