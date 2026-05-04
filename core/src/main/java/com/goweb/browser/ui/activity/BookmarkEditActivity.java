package com.goweb.browser.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goweb.browser.R;
import com.goweb.browser.utils.BookmarkUtils;

public class BookmarkEditActivity extends Activity {

    public static final String EXTRA_BOOKMARK_ID = "bookmark_id";
    public static final String EXTRA_BOOKMARK_TITLE = "bookmark_title";
    public static final String EXTRA_BOOKMARK_URL = "bookmark_url";

    private EditText titleEdit;
    private EditText urlEdit;
    private long bookmarkId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_edit);

        titleEdit = findViewById(R.id.edit_title);
        urlEdit = findViewById(R.id.edit_url);

        bookmarkId = getIntent().getLongExtra(EXTRA_BOOKMARK_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_BOOKMARK_TITLE);
        String url = getIntent().getStringExtra(EXTRA_BOOKMARK_URL);

        if (title != null) {
            titleEdit.setText(title);
        }
        if (url != null) {
            urlEdit.setText(url);
        }

        TextView titleView = findViewById(R.id.edit_title_label);
        if (bookmarkId >= 0) {
            titleView.setText(R.string.edit_bookmark);
        } else {
            titleView.setText(R.string.add_bookmark);
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> saveBookmark());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void saveBookmark() {
        String title = titleEdit.getText().toString().trim();
        String url = urlEdit.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(this, R.string.url_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookmarkId >= 0) {
            BookmarkUtils.updateBookmark(bookmarkId, title, url);
        } else {
            BookmarkUtils.addBookmark(title, url);
        }

        setResult(RESULT_OK);
        finish();
    }
}
