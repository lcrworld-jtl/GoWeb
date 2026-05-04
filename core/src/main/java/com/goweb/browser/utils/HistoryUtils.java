package com.goweb.browser.utils;

import android.content.SharedPreferences;

import com.goweb.browser.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryUtils {

    private static final String PREFS_NAME = "history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY = 500;

    public static class HistoryItem {
        public long id;
        public String title;
        public String url;
        public long visitTime;

        public HistoryItem(long id, String title, String url, long visitTime) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.visitTime = visitTime;
        }
    }

    private static SharedPreferences getPrefs() {
        return App.getContext().getSharedPreferences(PREFS_NAME, 0);
    }

    public static List<HistoryItem> getAllHistory() {
        List<HistoryItem> list = new ArrayList<>();
        try {
            String json = getPrefs().getString(KEY_HISTORY, "[]");
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new HistoryItem(
                        obj.optLong("id", i),
                        obj.optString("title", ""),
                        obj.optString("url", ""),
                        obj.optLong("visitTime", 0)
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void addHistory(String title, String url) {
        if (url == null || url.isEmpty() || url.startsWith("file:///")) {
            return;
        }
        List<HistoryItem> list = getAllHistory();
        long id = System.currentTimeMillis();
        list.add(0, new HistoryItem(id, title, url, id));
        if (list.size() > MAX_HISTORY) {
            list = list.subList(0, MAX_HISTORY);
        }
        saveHistory(list);
    }

    public static void deleteHistory(long id) {
        List<HistoryItem> list = getAllHistory();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id == id) {
                list.remove(i);
                break;
            }
        }
        saveHistory(list);
    }

    public static List<HistoryItem> searchHistory(String query) {
        List<HistoryItem> result = new ArrayList<>();
        List<HistoryItem> all = getAllHistory();
        String lowerQuery = query.toLowerCase();
        for (HistoryItem item : all) {
            if (item.title.toLowerCase().contains(lowerQuery) ||
                    item.url.toLowerCase().contains(lowerQuery)) {
                result.add(item);
            }
        }
        return result;
    }

    public static void clearAll() {
        getPrefs().edit().clear().apply();
    }

    public static List<String> getAutoCompleteUrls(String query) {
        List<String> result = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        List<HistoryItem> all = getAllHistory();
        String lowerQuery = query.toLowerCase();
        for (HistoryItem item : all) {
            if (item.url.toLowerCase().contains(lowerQuery) ||
                    item.title.toLowerCase().contains(lowerQuery)) {
                if (!result.contains(item.url)) {
                    result.add(item.url);
                }
                if (result.size() >= 5) {
                    break;
                }
            }
        }
        return result;
    }

    private static void saveHistory(List<HistoryItem> list) {
        try {
            JSONArray array = new JSONArray();
            for (HistoryItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("visitTime", item.visitTime);
                array.put(obj);
            }
            getPrefs().edit().putString(KEY_HISTORY, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
