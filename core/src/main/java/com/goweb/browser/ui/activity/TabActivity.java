package com.goweb.browser.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.goweb.browser.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TabActivity extends Activity {

    public static final String EXTRA_TAB_DATA = "tab_data";
    public static final String EXTRA_ACTIVE_INDEX = "active_index";
    public static final String RESULT_SWITCH_TO = "switch_to";
    public static final String RESULT_CLOSE = "close";
    public static final String RESULT_NEW_TAB = "new_tab";

    private ListView listView;
    private TabAdapter adapter;
    private List<TabInfo> tabList = new ArrayList<>();
    private int activeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViews();
        parseTabData();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.list_title);
        titleView.setText(R.string.tabs);

        ImageView addBtn = findViewById(R.id.btn_add);
        addBtn.setVisibility(View.VISIBLE);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(RESULT_NEW_TAB, true);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        ImageView closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        listView = findViewById(R.id.list_view);
        adapter = new TabAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra(RESULT_SWITCH_TO, position);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void parseTabData() {
        String data = getIntent().getStringExtra(EXTRA_TAB_DATA);
        activeIndex = getIntent().getIntExtra(EXTRA_ACTIVE_INDEX, 0);

        if (data == null || data.isEmpty()) {
            Toast.makeText(this, R.string.no_tabs, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                TabInfo info = new TabInfo();
                info.id = obj.optLong("id", 0);
                info.title = obj.optString("title", "New Tab");
                info.url = obj.optString("url", "");
                tabList.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.tabs_load_failed, Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }

    private class TabAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tabList.size();
        }

        @Override
        public TabInfo getItem(int position) {
            return tabList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(TabActivity.this).inflate(R.layout.item_tab, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.tab_icon);
                holder.title = convertView.findViewById(R.id.tab_title);
                holder.url = convertView.findViewById(R.id.tab_url);
                holder.close = convertView.findViewById(R.id.tab_close);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TabInfo info = tabList.get(position);
            holder.title.setText(info.title.isEmpty() ? "New Tab" : info.title);
            holder.url.setText(info.url.isEmpty() ? "" : info.url);

            if (position == activeIndex) {
                convertView.setBackgroundColor(0xFFE0F0E9);
            } else {
                convertView.setBackgroundColor(0xFFFFFFFF);
            }

            holder.close.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra(RESULT_CLOSE, position);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });

            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView url;
        ImageView close;
    }

    private static class TabInfo {
        long id;
        String title;
        String url;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        tabList.clear();
        super.onDestroy();
    }
}
