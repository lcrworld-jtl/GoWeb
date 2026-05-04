package com.goweb.browser.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goweb.browser.R;
import com.goweb.browser.utils.HistoryUtils;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<HistoryUtils.HistoryItem> history;

    public HistoryAdapter(Context context) {
        this.context = context;
        this.history = HistoryUtils.getAllHistory();
    }

    public void refresh() {
        this.history = HistoryUtils.getAllHistory();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public HistoryUtils.HistoryItem getItem(int position) {
        return history.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.history_icon);
            holder.title = convertView.findViewById(R.id.history_title);
            holder.url = convertView.findViewById(R.id.history_url);
            holder.time = convertView.findViewById(R.id.history_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HistoryUtils.HistoryItem item = history.get(position);
        holder.title.setText(item.title);
        holder.url.setText(item.url);
        holder.time.setText(formatTime(item.visitTime));

        // Generate site icon from URL domain
        Bitmap siteIcon = generateSiteIcon(item.url);
        holder.icon.setImageBitmap(siteIcon);

        return convertView;
    }

    private Bitmap generateSiteIcon(String url) {
        String domain = extractDomain(url);
        String letter = domain.isEmpty() ? "?" : domain.substring(0, 1).toUpperCase();

        int size = 80;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background circle with color based on domain
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getColorForDomain(domain));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Letter text
        paint.setColor(Color.WHITE);
        paint.setTextSize(size * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        paint.getTextBounds(letter, 0, letter.length(), textBounds);
        float textY = size / 2f + textBounds.height() / 2f;
        canvas.drawText(letter, size / 2f, textY, paint);

        return bitmap;
    }

    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            java.net.URL u = new java.net.URL(url);
            String host = u.getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            return "";
        }
    }

    private int getColorForDomain(String domain) {
        int hash = domain.hashCode();
        int[] colors = {
            0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1,
            0xFF3949AB, 0xFF1E88E5, 0xFF039BE5, 0xFF00ACC1,
            0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFC0CA33,
            0xFFFDD835, 0xFFFFB300, 0xFFFB8C00, 0xFFF4511E
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) {
            return context.getString(R.string.time_just_now);
        } else if (diff < 3600000) {
            return (diff / 60000) + " " + context.getString(R.string.time_min_ago);
        } else if (diff < 86400000) {
            return (diff / 3600000) + " " + context.getString(R.string.time_hours_ago);
        } else {
            return (diff / 86400000) + " " + context.getString(R.string.time_days_ago);
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView url;
        TextView time;
    }
}
