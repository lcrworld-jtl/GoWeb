package com.goweb.browser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.goweb.browser.ui.activity.BookmarkActivity;
import com.goweb.browser.ui.activity.BookmarkEditActivity;
import com.goweb.browser.ui.activity.DownloadActivity;
import com.goweb.browser.ui.activity.HistoryActivity;
import com.goweb.browser.ui.activity.SavedPagesActivity;
import com.goweb.browser.ui.activity.SettingsActivity;
import com.goweb.browser.ui.activity.TabActivity;
import com.goweb.browser.ui.adapter.SuggestionAdapter;
import com.goweb.browser.ui.dialog.CustomDialog;
import com.goweb.browser.utils.BookmarkUtils;
import com.goweb.browser.utils.DownloadUtils;
import com.goweb.browser.utils.HistoryUtils;
import com.goweb.browser.utils.SettingsUtils;
import com.goweb.browser.utils.UrlUtils;
import com.goweb.browser.webview.TabManager;
import com.goweb.browser.webview.WebViewManager;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends android.app.Activity implements WebViewManager.WebViewCallback {

    private static final int REQUEST_BOOKMARK = 1001;
    private static final int REQUEST_HISTORY = 1002;
    private static final int REQUEST_SETTINGS = 1003;
    private static final int REQUEST_TABS = 1004;
    private static final int REQUEST_SCAN_QR = 1005;
    private static final int REQUEST_CAMERA = 1006;
    private static final int REQUEST_STORAGE = 1007;
    private static final int REQUEST_FILE_CHOOSER = 8888;

    private TabManager tabManager;
    private EditText addressBar;
    private EditText homepageSearch;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private ImageView btnForward;
    private ImageView btnRefresh;
    private ImageView btnHome;
    private ImageView btnMenu;
    private ImageView btnBookmark;
    private ImageView btnTabs;
    private ImageView btnScan;
    private TextView tabCountView;
    private LinearLayout suggestionPanel;
    private ListView suggestionList;
    private SuggestionAdapter suggestionAdapter;
    private FrameLayout webViewContainer;
    private View touchInterceptor;
    private FrameLayout fullScreenContainer;
    private View homepageView;

    private String currentUrl = "";
    private String currentTitle = "";
    private boolean isPageLoading = false;
    private boolean showingHomepage = true;

    private CustomDialog activeDialog;
    private long lastBackTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        super.onCreate(savedInstanceState);
        applyNightMode();
        setContentView(R.layout.activity_main);

        initViews();
        initTabManager();
        initAddressBar();
        initToolbar();
        initSuggestionPanel();

        showHomepage();
        cleanupOldCache();
        handleIncomingIntent(getIntent());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_STORAGE);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            navigateTo(data.toString());
        } else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType())) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null && !sharedText.isEmpty()) {
                navigateTo(sharedText);
            }
        } else if (data != null && data.getScheme() != null) {
            String url = data.toString();
            if (url.startsWith("http://") || url.startsWith("https://")) {
                navigateTo(url);
            }
        }
    }

    private void applyNightMode() {
        if (SettingsUtils.isNightMode()) {
            setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        }
    }

    private void applyLanguage() {
        int lang = SettingsUtils.getLanguage();
        if (lang == SettingsUtils.LANG_AUTO) return;
        Locale locale;
        if (lang == SettingsUtils.LANG_CHINESE) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            locale = Locale.ENGLISH;
        }
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void initViews() {
        addressBar = findViewById(R.id.address_bar);
        homepageSearch = findViewById(R.id.homepage_search);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnHome = findViewById(R.id.btn_home);
        btnMenu = findViewById(R.id.btn_menu);
        btnBookmark = findViewById(R.id.btn_bookmark);
        btnTabs = findViewById(R.id.btn_tabs);
        btnScan = findViewById(R.id.btn_scan);
        tabCountView = findViewById(R.id.tab_count);
        suggestionPanel = findViewById(R.id.suggestion_panel);
        suggestionList = findViewById(R.id.suggestion_list);
        webViewContainer = findViewById(R.id.webview_container);
        touchInterceptor = findViewById(R.id.touch_interceptor);
        fullScreenContainer = findViewById(R.id.fullscreen_container);
        homepageView = findViewById(R.id.homepage_view);

        View quickBookmark = findViewById(R.id.quick_bookmark);
        View quickHistory = findViewById(R.id.quick_history);
        View quickDownload = findViewById(R.id.quick_download);
        View quickSettings = findViewById(R.id.quick_settings);

        if (quickBookmark != null) {
            quickBookmark.setOnClickListener(v -> {
                animateButton(v);
                openBookmarks();
            });
        }
        if (quickHistory != null) {
            quickHistory.setOnClickListener(v -> {
                animateButton(v);
                openHistory();
            });
        }
        if (quickDownload != null) {
            quickDownload.setOnClickListener(v -> {
                animateButton(v);
                openDownloads();
            });
        }
        if (quickSettings != null) {
            quickSettings.setOnClickListener(v -> {
                animateButton(v);
                openSettings();
            });
        }

        View btnChangelog = findViewById(R.id.btn_changelog);
        View btnOldVersions = findViewById(R.id.btn_old_versions);
        if (btnChangelog != null) {
            btnChangelog.setOnClickListener(v -> showChangelog());
        }
        if (btnOldVersions != null) {
            btnOldVersions.setOnClickListener(v -> showOldVersions());
        }
    }

    private void initTabManager() {
        tabManager = new TabManager(this, webViewContainer, this);
    }

    private WebViewManager getCurrentWebViewManager() {
        TabManager.Tab tab = tabManager.getActiveTab();
        return tab != null ? tab.webViewManager : null;
    }

    private void showHomepage() {
        showingHomepage = true;
        webViewContainer.setVisibility(View.GONE);
        homepageView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        btnRefresh.setImageResource(R.drawable.ic_refresh);
        isPageLoading = false;
        updateAddressBar("");
        updateNavigationButtons();
        animateHomepageEntrance();

        String bgPath = SettingsUtils.getHomepageBg();
        if (!bgPath.isEmpty()) {
            try {
                Bitmap bg = loadBitmapFromPath(bgPath);
                if (bg != null) {
                    homepageView.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bg));
                }
            } catch (Exception e) {
            }
        }

        ImageView homepageIcon = homepageView.findViewById(R.id.homepage_icon);
        String iconPath = SettingsUtils.getHomepageIcon();
        if (!iconPath.isEmpty()) {
            try {
                Bitmap icon = loadBitmapFromPath(iconPath);
                if (icon != null) {
                    homepageIcon.setImageBitmap(icon);
                } else {
                    homepageIcon.setImageResource(R.drawable.ic_globe);
                }
            } catch (Exception e) {
                homepageIcon.setImageResource(R.drawable.ic_globe);
            }
        } else {
            homepageIcon.setImageResource(R.drawable.ic_globe);
        }
    }

    private Bitmap loadBitmapFromPath(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("content://")) {
            try {
                java.io.InputStream is = getContentResolver().openInputStream(Uri.parse(path));
                if (is != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    return bitmap;
                }
            } catch (Exception e) {
            }
        } else {
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }

    private void hideHomepage() {
        showingHomepage = false;
        homepageView.setVisibility(View.GONE);
        webViewContainer.setVisibility(View.VISIBLE);
    }

    private void initAddressBar() {
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigateTo(addressBar.getText().toString().trim());
                return true;
            }
            return false;
        });

        addressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                addressBar.selectAll();
                showSuggestions(addressBar.getText().toString().trim());
            }
        });

        addressBar.setOnClickListener(v -> {
            if (addressBar.hasFocus()) {
                addressBar.selectAll();
            }
        });

        addressBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (addressBar.hasFocus()) showSuggestions(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        homepageSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = homepageSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    String url = UrlUtils.normalizeUrl(query);
                    homepageSearch.setText("");
                    hideKeyboard();
                    navigateTo(url);
                }
                return true;
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initToolbar() {
        btnBack.setOnClickListener(v -> {
            animateButton(v);
            if (showingHomepage) return;
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) wvm.goBack();
        });
        btnForward.setOnClickListener(v -> {
            animateButton(v);
            if (showingHomepage) return;
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) wvm.goForward();
        });
        btnRefresh.setOnClickListener(v -> {
            animateButton(v);
            if (showingHomepage) return;
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) {
                if (isPageLoading) wvm.stopLoading();
                else wvm.reload();
            }
        });
        btnHome.setOnClickListener(v -> {
            animateButton(v);
            showHomepage();
        });
        btnMenu.setOnClickListener(v -> {
            animateButton(v);
            showMainMenu();
        });
        btnBookmark.setOnClickListener(v -> {
            animateButton(v);
            toggleBookmark();
        });
        btnTabs.setOnClickListener(v -> {
            animateButton(v);
            openTabs();
        });
        btnScan.setOnClickListener(v -> {
            animateButton(v);
            scanQRCode();
        });
        updateNavigationButtons();
    }

    private void animateButton(View view) {
        view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start();
    }

    private void animateHomepageEntrance() {
        View icon = homepageView.findViewById(R.id.homepage_icon);
        View searchCard = homepageView.findViewById(R.id.homepage_search);
        if (icon != null) {
            icon.setAlpha(0f);
            icon.setTranslationY(-30f);
            icon.animate().alpha(1f).translationY(0f).setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        }
        if (searchCard != null) {
            searchCard.setAlpha(0f);
            searchCard.setTranslationY(20f);
            searchCard.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(150)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        }
    }

    private void initSuggestionPanel() {
        suggestionAdapter = new SuggestionAdapter(this);
        suggestionList.setAdapter(suggestionAdapter);
        suggestionList.setOnItemClickListener((parent, view, position, id) -> {
            String url = suggestionAdapter.getItem(position);
            navigateTo(url);
        });
        touchInterceptor.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideSuggestions();
                addressBar.clearFocus();
                hideKeyboard();
            }
            return false;
        });
    }

    private void navigateTo(String input) {
        if (input == null || input.isEmpty()) return;
        hideKeyboard();
        addressBar.clearFocus();
        hideSuggestions();
        String url = UrlUtils.normalizeUrl(input);

        if (tabManager.getTabCount() == 0) {
            tabManager.createTab(url);
        } else {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) wvm.loadUrl(url);
        }
        hideHomepage();
        updateTabCount();
    }

    private void showSuggestions(String query) {
        if (query.isEmpty()) {
            suggestionAdapter.setData(HistoryUtils.getAllHistory());
        } else {
            suggestionAdapter.setSearchData(HistoryUtils.searchHistory(query), BookmarkUtils.searchBookmarks(query));
        }
        suggestionPanel.setVisibility(View.VISIBLE);
        touchInterceptor.setVisibility(View.VISIBLE);
    }

    private void hideSuggestions() {
        suggestionPanel.setVisibility(View.GONE);
        touchInterceptor.setVisibility(View.GONE);
    }

    private void toggleBookmark() {
        if (currentUrl.isEmpty() || showingHomepage) return;
        if (BookmarkUtils.isBookmarked(currentUrl)) {
            BookmarkUtils.BookmarkItem item = findBookmarkByUrl(currentUrl);
            if (item != null) {
                BookmarkUtils.deleteBookmark(item.id);
                btnBookmark.setImageResource(R.drawable.ic_bookmark_outline);
                Toast.makeText(this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
            }
        } else {
            BookmarkUtils.addBookmark(currentTitle.isEmpty() ? currentUrl : currentTitle, currentUrl);
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
            Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
        }
    }

    private BookmarkUtils.BookmarkItem findBookmarkByUrl(String url) {
        for (BookmarkUtils.BookmarkItem item : BookmarkUtils.getAllBookmarks()) {
            if (item.url.equals(url)) return item;
        }
        return null;
    }

    private void showMainMenu() {
        String[] items = {
                getString(R.string.menu_new_tab),
                getString(R.string.menu_bookmarks),
                getString(R.string.menu_history),
                getString(R.string.menu_download),
                getString(R.string.saved_pages),
                getString(R.string.menu_add_bookmark),
                getString(R.string.find_in_page),
                getString(R.string.view_source),
                getString(R.string.add_to_desktop),
                getString(R.string.save_offline),
                getString(R.string.translate_page),
                getString(R.string.menu_share),
                getString(R.string.menu_settings)
        };
        int[] icons = {
                R.drawable.ic_file,
                R.drawable.ic_bookmark,
                R.drawable.ic_history,
                R.drawable.ic_cloud_download,
                R.drawable.ic_work,
                R.drawable.ic_bookmark_outline,
                R.drawable.ic_search,
                R.drawable.ic_insert_chart,
                R.drawable.ic_home,
                R.drawable.ic_work,
                R.drawable.ic_language,
                R.drawable.ic_link,
                R.drawable.ic_settings_gear
        };

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(8, 8, 8, 8);

        for (int i = 0; i < items.length; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_menu, container, false);
            ImageView iconView = itemView.findViewById(R.id.menu_item_icon);
            TextView textView = itemView.findViewById(R.id.menu_item_text);
            iconView.setImageResource(icons[i]);
            textView.setText(items[i]);
            final int pos = i;
            itemView.setOnClickListener(v -> {
                if (activeDialog != null) activeDialog.dismiss();
                handleMenuClick(pos);
            });
            container.addView(itemView);
        }

        activeDialog = new CustomDialog.Builder(this)
                .setTitle(null)
                .setView(container)
                .setCancelable(true)
                .show();
    }

    private void handleMenuClick(int position) {
        switch (position) {
            case 0:
                tabManager.createTab(SettingsUtils.getHomepage());
                hideHomepage();
                updateTabCount();
                break;
            case 1:
                openBookmarks();
                break;
            case 2:
                openHistory();
                break;
            case 3:
                openDownloads();
                break;
            case 4:
                viewSavedPages();
                break;
            case 5:
                toggleBookmark();
                break;
            case 6:
                findInPage();
                break;
            case 7:
                viewSource();
                break;
            case 8:
                addToDesktop();
                break;
            case 9:
                saveOffline();
                break;
            case 10:
                translatePage();
                break;
            case 11:
                sharePage();
                break;
            case 12:
                openSettings();
                break;
        }
    }

    private void scanQRCode() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                new CustomDialog.Builder(this)
                        .setTitle(getString(R.string.camera_permission_title))
                        .setMessage(getString(R.string.camera_permission_rationale))
                        .setPositiveButton(getString(R.string.btn_allow), () ->
                                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA))
                        .setNegativeButton(getString(R.string.btn_deny), null)
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
            return;
        }
        openQRScanner();
    }

    private void openQRScanner() {
        try {
            Intent intent = new Intent(this, com.goweb.browser.ui.activity.QRScanActivity.class);
            startActivityForResult(intent, REQUEST_SCAN_QR);
        } catch (Exception e) {
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, REQUEST_SCAN_QR);
            } catch (Exception e2) {
                Toast.makeText(this, R.string.camera_open_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openQRScanner();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.storage_permission_granted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void translatePage() {
        if (showingHomepage) return;
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return;
        WebView wv = wvm.getWebView();
        if (wv == null) return;

        String url = wv.getUrl();
        if (url == null || url.isEmpty()) return;

        String translateUrl = "https://www.microsofttranslator.com/bv.aspx?from=&to=zh-CHS&a=" + Uri.encode(url);
        wvm.loadUrl(translateUrl);
        Toast.makeText(this, R.string.translating, Toast.LENGTH_SHORT).show();
    }

    private void findInPage() {
        if (showingHomepage) return;
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 16, 8, 16);

        EditText input = new EditText(this);
        input.setHint(R.string.find_hint);
        input.setSingleLine();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        input.setLayoutParams(lp);
        layout.addView(input);

        TextView countText = new TextView(this);
        countText.setPadding(10, 0, 0, 0);
        countText.setTextSize(13);
        countText.setTextColor(0xFF666666);
        countText.setGravity(Gravity.CENTER_VERTICAL);
        layout.addView(countText);

        CustomDialog dialog = new CustomDialog.Builder(this)
                .setTitle(getString(R.string.find_in_page))
                .setView(layout)
                .setPositiveButton(getString(R.string.ok), () -> {
                    String q = input.getText().toString();
                    if (!q.isEmpty() && wvm.getWebView() != null) {
                        wvm.getWebView().findAllAsync(q);
                    }
                })
                .setNegativeButton(getString(R.string.close), null)
                .setCancelable(true)
                .show();
        activeDialog = dialog;

        if (wvm.getWebView() != null) {
            wvm.getWebView().setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
                if (isDoneCounting) {
                    if (numberOfMatches > 0) {
                        countText.setText(String.format(
                                getString(R.string.find_matches), activeMatchOrdinal + 1, numberOfMatches));
                    } else {
                        countText.setText(R.string.find_no_matches);
                    }
                }
            });
        }
    }

    private void viewSource() {
        if (showingHomepage) return;
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return;

        try {
            wvm.getWebView().evaluateJavascript(
                    "(function(){return document.documentElement.outerHTML;})()",
                    value -> {
                        if (value == null || value.length() < 3) {
                            Toast.makeText(this, R.string.view_source_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String html = value;
                        if (html.startsWith("\"")) html = html.substring(1);
                        if (html.endsWith("\"")) html = html.substring(0, html.length() - 1);
                        html = html.replace("\\\"", "\"")
                                .replace("\\n", "\n")
                                .replace("\\t", "\t")
                                .replace("\\u003C", "<")
                                .replace("\\u003E", ">");

                        String sourceHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                                "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                                "<style>body{font-family:monospace;font-size:12px;padding:12px;" +
                                "background:#1e1e1e;color:#d4d4d4;white-space:pre-wrap;" +
                                "word-wrap:break-word;}</style></head><body>" +
                                escapeHtmlForSource(html) + "</body></html>";

                        tabManager.createTab("");
                        WebViewManager sourceWvm = getCurrentWebViewManager();
                        if (sourceWvm != null) {
                            sourceWvm.getWebView().loadDataWithBaseURL(
                                    currentUrl, sourceHtml, "text/html", "UTF-8", null);
                        }
                        hideHomepage();
                        updateTabCount();
                    });
        } catch (Exception e) {
            Toast.makeText(this, R.string.view_source_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeHtmlForSource(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private void addToDesktop() {
        if (showingHomepage || currentUrl.isEmpty()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.content.pm.ShortcutManager sm = getSystemService(android.content.pm.ShortcutManager.class);
                if (sm != null && sm.isRequestPinShortcutSupported()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setData(Uri.parse(currentUrl));
                    intent.setAction(Intent.ACTION_VIEW);
                    android.content.pm.ShortcutInfo shortcut = new android.content.pm.ShortcutInfo.Builder(this, currentUrl)
                            .setShortLabel(currentTitle.isEmpty() ? "GoWeb" : currentTitle)
                            .setIntent(intent)
                            .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_globe))
                            .build();
                    sm.requestPinShortcut(shortcut, null);
                    Toast.makeText(this, R.string.shortcut_added, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.android_8_required, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOffline() {
        if (showingHomepage) return;
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return;

        try {
            File saveDir = new File(getFilesDir(), "saved_pages");
            if (!saveDir.exists()) saveDir.mkdirs();
            String safeName = currentTitle.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
            if (safeName.isEmpty() || safeName.length() > 30) safeName = "page";
            String fileName = safeName + "_" + System.currentTimeMillis() + ".mht";
            File outFile = new File(saveDir, fileName);
            wvm.getWebView().saveWebArchive(outFile.getAbsolutePath());
            Toast.makeText(this, getString(R.string.saved_offline) + ": " + safeName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.save_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void viewSavedPages() {
        Intent intent = new Intent(this, SavedPagesActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openBookmarks() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        startActivityForResult(intent, REQUEST_BOOKMARK);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivityForResult(intent, REQUEST_HISTORY);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openDownloads() {
        Intent intent = new Intent(this, DownloadActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openTabs() {
        if (tabManager.getTabCount() == 0) {
            tabManager.createTab(SettingsUtils.getHomepage());
        }
        Intent intent = new Intent(this, TabActivity.class);
        intent.putExtra(TabActivity.EXTRA_TAB_DATA, getTabsJson());
        intent.putExtra(TabActivity.EXTRA_ACTIVE_INDEX, tabManager.getActiveTabIndex());
        startActivityForResult(intent, REQUEST_TABS);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showChangelog() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.changelog_title))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void showOldVersions() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.version_history_title))
                .setMessage("v1.0.0")
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private String getTabsJson() {
        org.json.JSONArray array = new org.json.JSONArray();
        for (TabManager.Tab tab : tabManager.getAllTabs()) {
            org.json.JSONObject obj = new org.json.JSONObject();
            try {
                obj.put("id", tab.id);
                obj.put("title", tab.title == null ? "" : tab.title);
                obj.put("url", tab.url == null ? "" : tab.url);
                array.put(obj);
            } catch (org.json.JSONException e) {
            }
        }
        return array.toString();
    }

    private void sharePage() {
        if (currentUrl.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, currentTitle + " " + currentUrl);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_SETTINGS);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (showingHomepage) return;
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return;
        WebView webView = wvm.getWebView();
        if (webView == null) return;

        WebView.HitTestResult result = webView.getHitTestResult();
        if (result != null) {
            int type = result.getType();
            String extra = result.getExtra();
            if (type == WebView.HitTestResult.IMAGE_TYPE ||
                    type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                if (extra != null) {
                    menu.add(0, 1, 0, R.string.save_image);
                    menu.add(0, 2, 0, R.string.copy_image_url);
                }
            } else if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                if (extra != null) {
                    menu.add(0, 3, 0, R.string.copy_link);
                    menu.add(0, 4, 0, R.string.open_in_new_tab);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm == null) return super.onContextItemSelected(item);
        WebView webView = wvm.getWebView();
        if (webView == null) return super.onContextItemSelected(item);
        WebView.HitTestResult result = webView.getHitTestResult();
        if (result == null) return super.onContextItemSelected(item);
        String extra = result.getExtra();
        switch (item.getItemId()) {
            case 1:
                if (extra != null && !extra.isEmpty()) {
                    String mimeType = "image/*";
                    String lowerUrl = extra.toLowerCase();
                    if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) mimeType = "image/jpeg";
                    else if (lowerUrl.contains(".png")) mimeType = "image/png";
                    else if (lowerUrl.contains(".gif")) mimeType = "image/gif";
                    else if (lowerUrl.contains(".webp")) mimeType = "image/webp";
                    else if (lowerUrl.contains(".svg")) mimeType = "image/svg+xml";
                    else if (lowerUrl.contains(".bmp")) mimeType = "image/bmp";
                    DownloadUtils.download(this, extra, "", mimeType, wvm.getCookiesForUrl(extra));
                    Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
                }
                return true;
            case 2:
                if (extra != null) {
                    copyToClipboard(extra);
                    Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
                }
                return true;
            case 3:
                if (extra != null) {
                    copyToClipboard(extra);
                    Toast.makeText(this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
                return true;
            case 4:
                if (extra != null) {
                    tabManager.createTab(extra);
                    hideHomepage();
                    updateTabCount();
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    private void updateNavigationButtons() {
        if (showingHomepage) {
            btnBack.setAlpha(0.3f);
            btnForward.setAlpha(0.3f);
            return;
        }
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null) {
            btnBack.setAlpha(wvm.canGoBack() ? 1.0f : 0.3f);
            btnForward.setAlpha(wvm.canGoForward() ? 1.0f : 0.3f);
        }
    }

    private void updateAddressBar(String url) {
        if (addressBar.hasFocus()) return;
        currentUrl = url;
        addressBar.setText(url);
    }

    private void updateBookmarkIcon() {
        btnBookmark.setImageResource(
                BookmarkUtils.isBookmarked(currentUrl) ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_outline);
    }

    private void updateTabCount() {
        int count = tabManager.getTabCount();
        tabCountView.setText(String.valueOf(count));
        tabCountView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View focusView = getCurrentFocus();
            if (focusView != null) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onPageStarted(String url) {
        isPageLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setImageResource(R.drawable.ic_stop);
        updateAddressBar(url);
    }

    @Override
    public void onPageFinished(String url) {
        isPageLoading = false;
        progressBar.setVisibility(View.GONE);
        btnRefresh.setImageResource(R.drawable.ic_refresh);
        updateAddressBar(url);
        updateNavigationButtons();
        updateBookmarkIcon();

        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null && !SettingsUtils.isPrivacyMode()) {
            String title = wvm.getTitle();
            if (title != null && !title.isEmpty()) {
                currentTitle = title;
                HistoryUtils.addHistory(title, url);
            }
        }
        TabManager.Tab tab = tabManager.getActiveTab();
        if (tab != null) {
            tab.title = currentTitle;
            tab.url = url;
        }
    }

    @Override
    public void onProgressChanged(int newProgress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progressBar.setProgress(newProgress, true);
        } else {
            progressBar.setProgress(newProgress);
        }
        if (newProgress >= 100) progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onReceivedTitle(String title) {
        currentTitle = title != null ? title : "";
        TabManager.Tab tab = tabManager.getActiveTab();
        if (tab != null) tab.title = currentTitle;
    }

    @Override
    public void onReceivedIcon(Bitmap icon) {
        TabManager.Tab tab = tabManager.getActiveTab();
        if (tab != null) tab.favicon = icon;
    }

    @Override
    public void onError(int errorCode, String description, String failingUrl) {
        String errorTitle = getString(R.string.page_load_error);
        String errorDesc = getErrorDescription(errorCode, description);
        String errorHtml =
                "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                "<style>body{font-family:sans-serif;background:#f0f7f4;margin:0;padding:0;display:flex;justify-content:center;align-items:center;min-height:100vh;}" +
                ".container{background:#fff;border-radius:16px;padding:40px 30px;text-align:center;max-width:320px;width:90%;box-shadow:0 4px 20px rgba(45,106,79,0.08);}" +
                ".icon{font-size:64px;margin-bottom:16px;}h2{color:#2d6a4f;margin:0 0 12px 0;font-size:20px;}" +
                ".desc{color:#666;margin:0 0 24px 0;font-size:14px;line-height:1.5;}" +
                ".url{color:#999;margin:0 0 24px 0;font-size:12px;word-break:break-all;}" +
                ".btn{display:inline-block;padding:12px 32px;background:#2d6a4f;color:#fff;text-decoration:none;border-radius:24px;font-size:14px;}" +
                ".tips{margin-top:20px;padding-top:16px;border-top:1px solid #e0f0e9;text-align:left;}" +
                ".tips-title{color:#2d6a4f;font-size:12px;margin-bottom:8px;font-weight:bold;}" +
                ".tips-item{color:#888;font-size:11px;margin:4px 0;}" +
                "</style></head><body><div class='container'><div class='icon'>&#128683;</div>" +
                "<h2>" + errorTitle + "</h2><p class='desc'>" + errorDesc + "</p>" +
                "<p class='url'>" + escapeHtml(failingUrl) + "</p>" +
                "<a href='" + failingUrl + "' class='btn'>" + getString(R.string.retry) + "</a>" +
                "<div class='tips'><div class='tips-title'>" + getString(R.string.error_tips_title) + "</div>" +
                "<div class='tips-item'>1. " + getString(R.string.error_tip_1) + "</div>" +
                "<div class='tips-item'>2. " + getString(R.string.error_tip_2) + "</div>" +
                "<div class='tips-item'>3. " + getString(R.string.error_tip_3) + "</div></div></div></body></html>";
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null) {
            wvm.getWebView().loadDataWithBaseURL(failingUrl, errorHtml, "text/html", "UTF-8", failingUrl);
        }
    }

    private String getErrorDescription(int errorCode, String defaultDesc) {
        switch (errorCode) {
            case WebViewClient.ERROR_HOST_LOOKUP:
                return getString(R.string.error_host_lookup);
            case WebViewClient.ERROR_CONNECT:
                return getString(R.string.error_connect);
            case WebViewClient.ERROR_TIMEOUT:
                return getString(R.string.error_timeout);
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                return getString(R.string.error_ssl);
            default:
                return defaultDesc != null && !defaultDesc.isEmpty() ? defaultDesc : getString(R.string.error_unknown);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    @Override
    public void onDownloadStart(String url, String contentDisposition, String mimeType) {
        WebViewManager wvm = getCurrentWebViewManager();
        String cookies = wvm != null ? wvm.getCookiesForUrl(url) : "";
        DownloadUtils.download(this, url, contentDisposition, mimeType, cookies);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        fullScreenContainer.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullScreenContainer.setVisibility(View.VISIBLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    @Override
    public void onHideCustomView() {
        fullScreenContainer.removeAllViews();
        fullScreenContainer.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onFileChooserRequested(ValueCallback<Uri[]> filePathCallback) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_FILE_CHOOSER);
        } catch (Exception e) {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILE_CHOOSER) {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) {
                wvm.handleFileChooserResult(resultCode, data);
            }
            return;
        }

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_SCAN_QR) {
                String result = data.getStringExtra(com.goweb.browser.ui.activity.QRScanActivity.RESULT_QR_CODE);
                if (result != null && !result.isEmpty()) {
                    if (result.startsWith("http://") || result.startsWith("https://")) {
                        navigateTo(result);
                    } else {
                        showResultDialog(result);
                    }
                }
            } else if (requestCode == REQUEST_TABS) {
                if (data.hasExtra(TabActivity.RESULT_SWITCH_TO)) {
                    int index = data.getIntExtra(TabActivity.RESULT_SWITCH_TO, 0);
                    tabManager.switchToTab(index);
                    hideHomepage();
                } else if (data.hasExtra(TabActivity.RESULT_CLOSE)) {
                    int index = data.getIntExtra(TabActivity.RESULT_CLOSE, 0);
                    tabManager.closeTab(index);
                    if (tabManager.getTabCount() == 0) {
                        showHomepage();
                    }
                } else if (data.getBooleanExtra(TabActivity.RESULT_NEW_TAB, false)) {
                    tabManager.createTab(SettingsUtils.getHomepage());
                    hideHomepage();
                }
                updateTabCount();
                updateUIForCurrentTab();
            } else if (requestCode == REQUEST_BOOKMARK || requestCode == REQUEST_HISTORY) {
                String url = data.getStringExtra("url");
                if (url != null && !url.isEmpty()) navigateTo(url);
            }
        }

        if (requestCode == REQUEST_SETTINGS) {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) wvm.updateSettings();
            if (SettingsUtils.isNightModeChanged()) {
                recreate();
            } else if (showingHomepage) {
                showHomepage();
            }
        }
    }

    private void showResultDialog(String message) {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(8, 16, 8, 16);
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(15);
        tv.setTextColor(0xFF555555);
        layout.addView(tv);

        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(8, 8, 8, 8);

        TextView openBtn = new TextView(this);
        openBtn.setText(getString(R.string.btn_open));
        openBtn.setTextColor(0xFF2d6a4f);
        openBtn.setTextSize(14);
        openBtn.setPadding(16, 8, 16, 8);
        openBtn.setOnClickListener(v -> {
            if (activeDialog != null) activeDialog.dismiss();
            navigateTo(message);
        });
        btnLayout.addView(openBtn);

        TextView copyBtn = new TextView(this);
        copyBtn.setText(getString(R.string.btn_copy));
        copyBtn.setTextColor(0xFF2d6a4f);
        copyBtn.setTextSize(14);
        copyBtn.setPadding(16, 8, 16, 8);
        copyBtn.setOnClickListener(v -> {
            if (activeDialog != null) activeDialog.dismiss();
            copyToClipboard(message);
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
        });
        btnLayout.addView(copyBtn);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(layout);
        container.addView(btnLayout);

        activeDialog = new CustomDialog.Builder(this)
                .setTitle(getString(R.string.qr_scan_result))
                .setView(container)
                .setPositiveButton(getString(R.string.ok), null)
                .setCancelable(true)
                .show();
    }

    private void updateUIForCurrentTab() {
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null) {
            currentUrl = wvm.getUrl();
            currentTitle = wvm.getTitle();
            updateAddressBar(currentUrl);
            updateNavigationButtons();
            updateBookmarkIcon();

            WebView webView = wvm.getWebView();
            if (webView != null) {
                registerForContextMenu(webView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
            return;
        }
        if (fullScreenContainer.getVisibility() == View.VISIBLE) {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null && wvm.isInCustomView()) wvm.hideCustomView();
            return;
        }
        if (suggestionPanel.getVisibility() == View.VISIBLE) {
            hideSuggestions();
            return;
        }
        if (!showingHomepage) {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null && wvm.canGoBack()) {
                wvm.goBack();
                return;
            }
            showHomepage();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastBackTime < 2000) {
            finish();
        } else {
            lastBackTime = now;
            Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
        }
    }

    private void cleanupOldCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                long maxSize = 50 * 1024 * 1024;
                if (getDirSize(cacheDir) > maxSize) {
                    deleteDirContents(cacheDir);
                }
            }
        } catch (Exception e) {
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) size += getDirSize(f);
                else size += f.length();
            }
        }
        return size;
    }

    private void deleteDirContents(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirContents(f);
                }
                f.delete();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null) wvm.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WebViewManager wvm = getCurrentWebViewManager();
        if (wvm != null) wvm.onResume();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (SettingsUtils.isPrivacyMode()) {
            WebViewManager wvm = getCurrentWebViewManager();
            if (wvm != null) wvm.clearAllData();
            cleanupOldCache();
        }
        tabManager.closeAllTabs();
        activeDialog = null;
        super.onDestroy();
    }
}
