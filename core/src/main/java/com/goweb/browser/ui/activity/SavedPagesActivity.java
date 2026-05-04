package com.goweb.browser.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.goweb.browser.R;
import com.goweb.browser.ui.dialog.CustomDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedPagesActivity extends Activity {

    private ListView listView;
    private SavedPagesAdapter adapter;
    private List<File> savedFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViews();
        loadSavedPages();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.list_title);
        titleView.setText(R.string.saved_pages);

        ImageView closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> finish());

        ImageView addBtn = findViewById(R.id.btn_add);
        addBtn.setVisibility(View.GONE);

        listView = findViewById(R.id.list_view);
        adapter = new SavedPagesAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            File file = savedFiles.get(position);
            Intent intent = new Intent(this, com.goweb.browser.MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.fromFile(file));
            startActivity(intent);
            finish();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            File file = savedFiles.get(position);
            new CustomDialog.Builder(this)
                    .setTitle(getString(R.string.delete))
                    .setMessage(file.getName())
                    .setPositiveButton(getString(R.string.delete), () -> {
                        file.delete();
                        loadSavedPages();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
    }

    private void loadSavedPages() {
        savedFiles.clear();
        try {
            File saveDir = new File(getFilesDir(), "saved_pages");
            if (saveDir.exists() && saveDir.isDirectory()) {
                File[] files = saveDir.listFiles((dir, name) -> name.endsWith(".mht"));
                if (files != null) {
                    for (File f : files) savedFiles.add(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedFiles.isEmpty()) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.empty_view)).setText(R.string.no_saved_pages);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private class SavedPagesAdapter extends BaseAdapter {
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        @Override
        public int getCount() { return savedFiles.size(); }

        @Override
        public File getItem(int pos) { return savedFiles.get(pos); }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_history, parent, false);
            }
            File file = savedFiles.get(pos);
            TextView title = convertView.findViewById(R.id.history_title);
            TextView url = convertView.findViewById(R.id.history_url);
            TextView time = convertView.findViewById(R.id.history_time);

            title.setText(file.getName().replace(".mht", ""));
            url.setText(file.getAbsolutePath());
            time.setText(sdf.format(new Date(file.lastModified())));

            convertView.findViewById(R.id.history_time).setVisibility(View.VISIBLE);

            return convertView;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedPages();
    }
}
