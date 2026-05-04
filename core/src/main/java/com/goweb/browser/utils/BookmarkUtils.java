package com.goweb.browser.utils;

import android.content.SharedPreferences;

import com.goweb.browser.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarkUtils {

    private static final String PREFS_NAME = "bookmarks";
    private static final String KEY_BOOKMARKS = "bookmark_list";

    public static class BookmarkItem {
        public long id;
        public String title;
        public String url;
        public long createTime;

        public BookmarkItem(long id, String title, String url, long createTime) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.createTime = createTime;
        }
    }

    private static SharedPreferences getPrefs() {
        return App.getContext().getSharedPreferences(PREFS_NAME, 0);
    }

    public static List<BookmarkItem> getAllBookmarks() {
        List<BookmarkItem> list = new ArrayList<>();
        try {
            String json = getPrefs().getString(KEY_BOOKMARKS, "[]");
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new BookmarkItem(
                        obj.optLong("id", i),
                        obj.optString("title", ""),
                        obj.optString("url", ""),
                        obj.optLong("createTime", 0)
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void addBookmark(String title, String url) {
        List<BookmarkItem> list = getAllBookmarks();
        long id = System.currentTimeMillis();
        list.add(new BookmarkItem(id, title, url, id));
        saveBookmarks(list);
    }

    public static void updateBookmark(long id, String title, String url) {
        List<BookmarkItem> list = getAllBookmarks();
        for (BookmarkItem item : list) {
            if (item.id == id) {
                item.title = title;
                item.url = url;
                break;
            }
        }
        saveBookmarks(list);
    }

    public static void deleteBookmark(long id) {
        List<BookmarkItem> list = getAllBookmarks();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).id == id) {
                list.remove(i);
                break;
            }
        }
        saveBookmarks(list);
    }

    public static boolean isBookmarked(String url) {
        List<BookmarkItem> list = getAllBookmarks();
        for (BookmarkItem item : list) {
            if (item.url.equals(url)) {
                return true;
            }
        }
        return false;
    }

    public static List<BookmarkItem> searchBookmarks(String query) {
        List<BookmarkItem> result = new ArrayList<>();
        List<BookmarkItem> all = getAllBookmarks();
        String lowerQuery = query.toLowerCase();
        for (BookmarkItem item : all) {
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

    private static void saveBookmarks(List<BookmarkItem> list) {
        try {
            JSONArray array = new JSONArray();
            for (BookmarkItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("url", item.url);
                obj.put("createTime", item.createTime);
                array.put(obj);
            }
            getPrefs().edit().putString(KEY_BOOKMARKS, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
