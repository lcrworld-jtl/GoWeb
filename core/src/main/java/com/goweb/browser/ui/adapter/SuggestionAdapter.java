package com.goweb.browser.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goweb.browser.R;
import com.goweb.browser.utils.BookmarkUtils;
import com.goweb.browser.utils.HistoryUtils;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends BaseAdapter {

    private static final int TYPE_HISTORY = 0;
    private static final int TYPE_BOOKMARK = 1;

    private final Context context;
    private List<Item> items = new ArrayList<>();

    private static class Item {
        String title;
        String url;
        int type;

        Item(String title, String url, int type) {
            this.title = title;
            this.url = url;
            this.type = type;
        }
    }

    public SuggestionAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<HistoryUtils.HistoryItem> historyItems) {
        items.clear();
        for (HistoryUtils.HistoryItem item : historyItems) {
            items.add(new Item(item.title, item.url, TYPE_HISTORY));
        }
        notifyDataSetChanged();
    }

    public void setSearchData(List<HistoryUtils.HistoryItem> historyItems,
                              List<BookmarkUtils.BookmarkItem> bookmarkItems) {
        items.clear();
        for (BookmarkUtils.BookmarkItem item : bookmarkItems) {
            items.add(new Item(item.title, item.url, TYPE_BOOKMARK));
        }
        for (HistoryUtils.HistoryItem item : historyItems) {
            items.add(new Item(item.title, item.url, TYPE_HISTORY));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position).url;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_suggestion, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.suggestion_icon);
            holder.title = convertView.findViewById(R.id.suggestion_title);
            holder.url = convertView.findViewById(R.id.suggestion_url);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = items.get(position);
        holder.icon.setImageResource(item.type == TYPE_BOOKMARK ?
                R.drawable.ic_bookmark : R.drawable.ic_history);
        holder.title.setText(item.title.isEmpty() ? item.url : item.title);
        holder.url.setText(item.url);

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView url;
    }
}
