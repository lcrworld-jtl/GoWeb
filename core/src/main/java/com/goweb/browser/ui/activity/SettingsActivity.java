package com.goweb.browser.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.goweb.browser.R;
import com.goweb.browser.ui.dialog.CustomDialog;
import com.goweb.browser.utils.BookmarkUtils;
import com.goweb.browser.utils.HistoryUtils;
import com.goweb.browser.utils.SettingsUtils;

public class SettingsActivity extends android.app.Activity {

    private static final int REQUEST_PICK_BG = 2001;
    private static final int REQUEST_PICK_ICON = 2002;
    private static final int REQUEST_PICK_FONT = 2003;
    private static final int REQUEST_STORAGE = 2004;

    private ListView settingsList;
    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.settings_title);
        titleView.setText(R.string.menu_settings);

        settingsList = findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this);
        settingsList.setAdapter(adapter);
        settingsList.setOnItemClickListener(this::onSettingItemClick);

        // Animate list items
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        settingsList.setLayoutAnimation(new android.view.animation.LayoutAnimationController(fadeIn, 0.05f));
    }

    private void onSettingItemClick(AdapterView<?> parent, View view, int position, long id) {
        SettingsAdapter.SettingItem item = adapter.getItem(position);
        if (item == null) return;

        switch (item.type) {
            case SettingsAdapter.TYPE_SEARCH_ENGINE:
                showSearchEngineDialog();
                break;
            case SettingsAdapter.TYPE_HOMEPAGE:
                showHomepageDialog();
                break;
            case SettingsAdapter.TYPE_USER_AGENT:
                showUserAgentDialog();
                break;
            case SettingsAdapter.TYPE_FONT_SIZE:
                showFontSizeDialog();
                break;
            case SettingsAdapter.TYPE_LANGUAGE:
                showLanguageDialog();
                break;
            case SettingsAdapter.TYPE_HOMEPAGE_BG:
                pickHomepageBg();
                break;
            case SettingsAdapter.TYPE_HOMEPAGE_ICON:
                pickHomepageIcon();
                break;
            case SettingsAdapter.TYPE_CUSTOM_FONT:
                pickCustomFont();
                break;
            case SettingsAdapter.TYPE_SET_DEFAULT:
                setDefaultBrowser();
                break;
            case SettingsAdapter.TYPE_TOGGLE:
                toggleSetting(item.key);
                break;
            case SettingsAdapter.TYPE_CLEAR_CACHE:
                clearCache();
                break;
            case SettingsAdapter.TYPE_CLEAR_HISTORY:
                clearHistory();
                break;
            case SettingsAdapter.TYPE_CLEAR_COOKIES:
                clearCookies();
                break;
            case SettingsAdapter.TYPE_CLEAR_ALL:
                clearAllData();
                break;
            case SettingsAdapter.TYPE_ABOUT:
                showAbout();
                break;
        }
    }

    private void toggleSetting(String key) {
        boolean newState = false;
        switch (key) {
            case "night_mode":
                newState = !SettingsUtils.isNightMode();
                SettingsUtils.setNightMode(newState);
                Toast.makeText(this, newState ? R.string.night_mode_on : R.string.night_mode_off,
                        Toast.LENGTH_SHORT).show();
                break;
            case "privacy_mode":
                newState = !SettingsUtils.isPrivacyMode();
                SettingsUtils.setPrivacyMode(newState);
                Toast.makeText(this, newState ? R.string.privacy_mode_on : R.string.privacy_mode_off,
                        Toast.LENGTH_SHORT).show();
                break;
            case "enable_js":
                newState = !SettingsUtils.isEnableJs();
                SettingsUtils.setEnableJs(newState);
                Toast.makeText(this, newState ? R.string.js_enabled : R.string.js_disabled,
                        Toast.LENGTH_SHORT).show();
                break;
            case "enable_cookies":
                newState = !SettingsUtils.isEnableCookies();
                SettingsUtils.setEnableCookies(newState);
                Toast.makeText(this, newState ? R.string.cookies_enabled : R.string.cookies_disabled,
                        Toast.LENGTH_SHORT).show();
                break;
            case "block_images":
                newState = !SettingsUtils.isBlockImages();
                SettingsUtils.setBlockImages(newState);
                Toast.makeText(this, newState ? R.string.images_blocked : R.string.images_allowed,
                        Toast.LENGTH_SHORT).show();
                break;
        }
        adapter.refreshData();
    }

    private void showSearchEngineDialog() {
        String[] names = SettingsUtils.getSearchEngineNames();
        int current = SettingsUtils.getSearchEngine();
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_search_engine))
                .setItems(names, which -> {
                    SettingsUtils.setSearchEngine(which);
                    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                    adapter.refreshData();
                })
                .setSelectedIndex(current)
                .show();
    }

    private void showHomepageDialog() {
        EditText input = new EditText(this);
        input.setText(SettingsUtils.getHomepage());
        input.setSingleLine();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(input);

        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_homepage))
                .setView(container)
                .setPositiveButton(getString(R.string.ok), () -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        SettingsUtils.setHomepage(url);
                        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showUserAgentDialog() {
        String[] options = {getString(R.string.ua_mobile), getString(R.string.ua_pc), getString(R.string.ua_custom)};
        int current = SettingsUtils.getUaMode();
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_user_agent))
                .setItems(options, which -> {
                    if (which == 2) {
                        showCustomUaDialog();
                    } else {
                        SettingsUtils.setUaMode(which);
                        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                        adapter.refreshData();
                    }
                })
                .setSelectedIndex(current)
                .show();
    }

    private void showCustomUaDialog() {
        EditText input = new EditText(this);
        input.setText(SettingsUtils.getCustomUa());
        input.setSingleLine();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(input);

        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.ua_custom))
                .setView(container)
                .setPositiveButton(getString(R.string.ok), () -> {
                    String ua = input.getText().toString().trim();
                    if (!ua.isEmpty()) {
                        SettingsUtils.setUaMode(SettingsUtils.UA_CUSTOM);
                        SettingsUtils.setCustomUa(ua);
                        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                        adapter.refreshData();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showFontSizeDialog() {
        String[] sizes = {"80%", "90%", "100%", "110%", "120%", "150%"};
        int[] values = {80, 90, 100, 110, 120, 150};
        int current = SettingsUtils.getFontSize();
        int selected = 2;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                selected = i;
                break;
            }
        }
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_font_size))
                .setItems(sizes, which -> {
                    SettingsUtils.setFontSize(values[which]);
                    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                    adapter.refreshData();
                })
                .setSelectedIndex(selected)
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.language_auto), getString(R.string.language_english), getString(R.string.language_chinese)};
        int current = SettingsUtils.getLanguage();
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_language))
                .setItems(languages, which -> {
                    SettingsUtils.setLanguage(which);
                    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .setSelectedIndex(current)
                .show();
    }

    private void pickHomepageBg() {
        String current = SettingsUtils.getHomepageBg();
        String[] options;
        if (current.isEmpty()) {
            options = new String[]{getString(R.string.select_image)};
        } else {
            options = new String[]{getString(R.string.select_image), getString(R.string.restore_default)};
        }
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_homepage_bg))
                .setItems(options, which -> {
                    if (which == 0) {
                        if (!checkStoragePermission()) return;
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_PICK_BG);
                    } else if (which == 1) {
                        SettingsUtils.setHomepageBg("");
                        Toast.makeText(this, R.string.restored_default, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void pickHomepageIcon() {
        String current = SettingsUtils.getHomepageIcon();
        String[] options;
        if (current.isEmpty()) {
            options = new String[]{getString(R.string.select_image)};
        } else {
            options = new String[]{getString(R.string.select_image), getString(R.string.restore_default)};
        }
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_homepage_icon))
                .setItems(options, which -> {
                    if (which == 0) {
                        if (!checkStoragePermission()) return;
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_PICK_ICON);
                    } else if (which == 1) {
                        SettingsUtils.setHomepageIcon("");
                        Toast.makeText(this, R.string.restored_default, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void pickCustomFont() {
        if (!checkStoragePermission()) return;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-opentype"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_PICK_FONT);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new CustomDialog.Builder(this)
                            .setTitle(getString(R.string.storage_permission_title))
                            .setMessage(getString(R.string.storage_permission_rationale))
                            .setPositiveButton(getString(R.string.btn_allow), () ->
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE))
                            .setNegativeButton(getString(R.string.btn_deny), null)
                            .show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                }
                return false;
            }
        }
        return true;
    }

    private void setDefaultBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://www.example.com"));
            Intent chooser = Intent.createChooser(intent, getString(R.string.settings_set_default));
            startActivity(chooser);
        }
    }

    private void clearCache() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_clear_cache))
                .setMessage(getString(R.string.confirm_clear_cache))
                .setPositiveButton(getString(R.string.ok), () -> {
                    SettingsUtils.clearCache();
                    Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void clearHistory() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_clear_history))
                .setMessage(getString(R.string.confirm_clear_history))
                .setPositiveButton(getString(R.string.ok), () -> {
                    HistoryUtils.clearAll();
                    Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void clearCookies() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_clear_cookies))
                .setMessage(getString(R.string.confirm_clear_cookies))
                .setPositiveButton(getString(R.string.ok), () -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().removeAllCookies(null);
                        CookieManager.getInstance().flush();
                    } else {
                        CookieSyncManager.createInstance(this);
                        CookieManager.getInstance().removeAllCookie();
                        CookieSyncManager.getInstance().sync();
                    }
                    Toast.makeText(this, R.string.cookies_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void clearAllData() {
        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.settings_clear_all))
                .setMessage(getString(R.string.confirm_clear_all))
                .setPositiveButton(getString(R.string.ok), () -> {
                    SettingsUtils.clearAllData();
                    HistoryUtils.clearAll();
                    BookmarkUtils.clearAll();
                    Toast.makeText(this, R.string.all_data_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showAbout() {
        new CustomDialog.Builder(this)
                .setTitle("GoWeb")
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {}
                String path = uri.toString();
                switch (requestCode) {
                    case REQUEST_PICK_BG:
                        SettingsUtils.setHomepageBg(path);
                        Toast.makeText(this, R.string.bg_set, Toast.LENGTH_SHORT).show();
                        break;
                    case REQUEST_PICK_ICON:
                        SettingsUtils.setHomepageIcon(path);
                        Toast.makeText(this, R.string.icon_set, Toast.LENGTH_SHORT).show();
                        break;
                    case REQUEST_PICK_FONT:
                        SettingsUtils.setCustomFont(path);
                        Toast.makeText(this, R.string.font_set, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.storage_permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.refreshData();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
