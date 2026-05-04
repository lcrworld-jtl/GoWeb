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
import com.goweb.browser.ui.adapter.BookmarkAdapter;
import com.goweb.browser.utils.BookmarkUtils;

import java.util.List;

public class BookmarkActivity extends Activity {

    private ListView listView;
    private BookmarkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initViews();
        loadData();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.list_title);
        titleView.setText(R.string.menu_bookmarks);

        ImageView addBtn = findViewById(R.id.btn_add);
        addBtn.setVisibility(View.VISIBLE);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookmarkEditActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        ImageView closeBtn = findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        listView = findViewById(R.id.list_view);
        adapter = new BookmarkAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BookmarkUtils.BookmarkItem item = adapter.getItem(position);
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
        List<BookmarkUtils.BookmarkItem> bookmarks = BookmarkUtils.getAllBookmarks();
        if (bookmarks.isEmpty()) {
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
