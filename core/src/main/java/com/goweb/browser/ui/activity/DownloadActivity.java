package com.goweb.browser.ui.activity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.goweb.browser.R;
import com.goweb.browser.ui.dialog.CustomDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadActivity extends Activity {

    private ListView listView;
    private DownloadAdapter adapter;
    private TextView emptyView;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViews();
        loadDownloads();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.list_title);
        titleView.setText(R.string.menu_download);

        ImageView backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(v -> {
            animateButton(v);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        listView = findViewById(R.id.list_view);
        emptyView = findViewById(R.id.empty_view);
        loadingView = findViewById(R.id.loading_view);

        adapter = new DownloadAdapter();
        listView.setAdapter(adapter);

        // Animate list items
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        listView.setLayoutAnimation(new android.view.animation.LayoutAnimationController(fadeIn, 0.1f));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            DownloadItem item = adapter.getItem(position);
            openDownload(item);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            DownloadItem item = adapter.getItem(position);
            showDeleteDialog(item);
            return true;
        });
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void loadDownloads() {
        loadingView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        List<DownloadItem> downloads = queryDownloads();

        loadingView.setVisibility(View.GONE);

        if (downloads.isEmpty()) {
            emptyView.setText(R.string.no_downloads);
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            adapter.updateData(downloads);
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            // Restart layout animation
            listView.scheduleLayoutAnimation();
        }
    }

    private List<DownloadItem> queryDownloads() {
        List<DownloadItem> list = new ArrayList<>();
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (manager == null) return list;

        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = manager.query(query);
        if (cursor == null) return list;

        int idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
        int titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
        int descIndex = cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION);
        int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int dateIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
        int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
        int bytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
        int totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idIndex);
            String title = cursor.getString(titleIndex);
            String description = cursor.getString(descIndex);
            String uri = cursor.getString(uriIndex);
            int status = cursor.getInt(statusIndex);
            long date = cursor.getLong(dateIndex);
            String localUri = cursor.getString(localUriIndex);
            long bytes = cursor.getLong(bytesIndex);
            long total = cursor.getLong(totalIndex);

            // Only show downloads from GoWeb
            if (description != null && description.contains("GoWeb")) {
                list.add(new DownloadItem(id, title, uri, status, date, localUri, bytes, total));
            }
        }
        cursor.close();
        return list;
    }

    private void openDownload(DownloadItem item) {
        if (item.localUri == null || item.localUri.isEmpty()) {
            Toast.makeText(this, R.string.download_not_complete, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(item.localUri), getMimeType(item.title));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog(DownloadItem item) {
        new CustomDialog.Builder(this)
                .setTitle(item.title)
                .setMessage(getString(R.string.confirm_delete_download))
                .setPositiveButton(getString(R.string.delete), () -> {
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (manager != null) {
                        manager.remove(item.id);
                        loadDownloads();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".mp4")) return "video/mp4";
        if (fileName.endsWith(".mp3")) return "audio/mpeg";
        if (fileName.endsWith(".apk")) return "application/vnd.android.package-archive";
        if (fileName.endsWith(".txt")) return "text/plain";
        return "*/*";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDownloads();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private static class DownloadItem {
        long id;
        String title;
        String uri;
        int status;
        long date;
        String localUri;
        long bytes;
        long total;

        DownloadItem(long id, String title, String uri, int status, long date, String localUri, long bytes, long total) {
            this.id = id;
            this.title = title;
            this.uri = uri;
            this.status = status;
            this.date = date;
            this.localUri = localUri;
            this.bytes = bytes;
            this.total = total;
        }
    }

    private class DownloadAdapter extends BaseAdapter {
        private List<DownloadItem> items = new ArrayList<>();

        void updateData(List<DownloadItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public DownloadItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_download, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.download_icon);
                holder.title = convertView.findViewById(R.id.download_title);
                holder.url = convertView.findViewById(R.id.download_url);
                holder.status = convertView.findViewById(R.id.download_status);
                holder.size = convertView.findViewById(R.id.download_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DownloadItem item = items.get(position);
            holder.title.setText(item.title);
            holder.url.setText(item.uri);

            // Status
            String statusText;
            int statusColor;
            switch (item.status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusText = getString(R.string.download_success);
                    statusColor = 0xFF2d6a4f;
                    break;
                case DownloadManager.STATUS_PENDING:
                    statusText = getString(R.string.download_pending);
                    statusColor = 0xFF52b788;
                    break;
                case DownloadManager.STATUS_RUNNING:
                    statusText = getString(R.string.download_running);
                    statusColor = 0xFF2d6a4f;
                    break;
                case DownloadManager.STATUS_PAUSED:
                    statusText = getString(R.string.download_paused);
                    statusColor = 0xFF9E9E9E;
                    break;
                case DownloadManager.STATUS_FAILED:
                    statusText = getString(R.string.download_failed);
                    statusColor = 0xFFc0392b;
                    break;
                default:
                    statusText = getString(R.string.download_unknown);
                    statusColor = 0xFF9E9E9E;
            }
            holder.status.setText(statusText);
            holder.status.setTextColor(statusColor);

            // Size
            if (item.total > 0) {
                holder.size.setText(formatSize(item.bytes) + " / " + formatSize(item.total));
            } else {
                holder.size.setText(formatSize(item.bytes));
            }

            // Icon based on file type
            holder.icon.setImageResource(getFileIcon(item.title));

            return convertView;
        }

        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }

        private int getFileIcon(String fileName) {
            if (fileName.endsWith(".pdf")) return R.drawable.ic_file;
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) return R.drawable.ic_file;
            if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mkv")) return R.drawable.ic_file;
            if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".flac")) return R.drawable.ic_file;
            if (fileName.endsWith(".apk")) return R.drawable.ic_file;
            if (fileName.endsWith(".zip") || fileName.endsWith(".rar") || fileName.endsWith(".7z")) return R.drawable.ic_file;
            return R.drawable.ic_file;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView url;
        TextView status;
        TextView size;
    }
}
