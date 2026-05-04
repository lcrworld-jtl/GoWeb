package com.goweb.browser.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.goweb.browser.R;
import com.goweb.browser.ui.adapter.HistoryAdapter;
import com.goweb.browser.utils.HistoryUtils;

import java.util.List;

public class HistoryActivity extends Activity {

    private ListView listView;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViews();
        loadData();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.list_title);
        titleView.setText(R.string.menu_history);

        ImageView clearBtn = findViewById(R.id.btn_add);
        clearBtn.setVisibility(View.VISIBLE);
        clearBtn.setImageResource(R.drawable.ic_close);
        clearBtn.setOnClickListener(v -> {
            HistoryUtils.clearAll();
            loadData();
        });

        ImageView closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        listView = findViewById(R.id.list_view);
        adapter = new HistoryAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            HistoryUtils.HistoryItem item = adapter.getItem(position);
            if (item != null) {
                Intent intent = new Intent();
                intent.putExtra("url", item.url);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void loadData() {
        List<HistoryUtils.HistoryItem> history = HistoryUtils.getAllHistory();
        if (history.isEmpty()) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.GONE);
            adapter.refresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
