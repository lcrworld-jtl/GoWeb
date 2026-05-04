package com.goweb.browser.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.goweb.browser.R;
import com.goweb.browser.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends BaseAdapter {

    public static final int TYPE_SEARCH_ENGINE = 0;
    public static final int TYPE_HOMEPAGE = 1;
    public static final int TYPE_USER_AGENT = 2;
    public static final int TYPE_FONT_SIZE = 3;
    public static final int TYPE_LANGUAGE = 4;
    public static final int TYPE_HOMEPAGE_BG = 5;
    public static final int TYPE_HOMEPAGE_ICON = 6;
    public static final int TYPE_CUSTOM_FONT = 7;
    public static final int TYPE_SET_DEFAULT = 8;
    public static final int TYPE_TOGGLE = 9;
    public static final int TYPE_CLEAR_CACHE = 10;
    public static final int TYPE_CLEAR_HISTORY = 11;
    public static final int TYPE_CLEAR_COOKIES = 12;
    public static final int TYPE_CLEAR_ALL = 13;
    public static final int TYPE_ABOUT = 14;

    private Context context;
    private List<SettingItem> items = new ArrayList<>();

    public static class SettingItem {
        public String title;
        public String subtitle;
        public int type;
        public String key;

        public SettingItem(String title, String subtitle, int type, String key) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
            this.key = key;
        }
    }

    public SettingsAdapter(Context context) {
        this.context = context;
        refreshData();
    }

    public void refreshData() {
        items.clear();
        items.add(new SettingItem(context.getString(R.string.settings_search_engine), getSearchEngineName(), TYPE_SEARCH_ENGINE, null));
        items.add(new SettingItem(context.getString(R.string.settings_homepage), SettingsUtils.getHomepage(), TYPE_HOMEPAGE, null));
        items.add(new SettingItem(context.getString(R.string.settings_user_agent), getUserAgentName(), TYPE_USER_AGENT, null));
        items.add(new SettingItem(context.getString(R.string.settings_font_size), SettingsUtils.getFontSize() + "%", TYPE_FONT_SIZE, null));
        items.add(new SettingItem(context.getString(R.string.settings_language), getLanguageName(), TYPE_LANGUAGE, null));
        items.add(new SettingItem(context.getString(R.string.settings_homepage_bg), "", TYPE_HOMEPAGE_BG, null));
        items.add(new SettingItem(context.getString(R.string.settings_homepage_icon), "", TYPE_HOMEPAGE_ICON, null));
        items.add(new SettingItem(context.getString(R.string.settings_custom_font), "", TYPE_CUSTOM_FONT, null));
        items.add(new SettingItem(context.getString(R.string.settings_set_default), "", TYPE_SET_DEFAULT, null));
        items.add(new SettingItem(context.getString(R.string.settings_night_mode), getToggleState(SettingsUtils.isNightMode()), TYPE_TOGGLE, "night_mode"));
        items.add(new SettingItem(context.getString(R.string.settings_privacy_mode), getToggleState(SettingsUtils.isPrivacyMode()), TYPE_TOGGLE, "privacy_mode"));
        items.add(new SettingItem(context.getString(R.string.settings_enable_js), getToggleState(SettingsUtils.isEnableJs()), TYPE_TOGGLE, "enable_js"));
        items.add(new SettingItem(context.getString(R.string.settings_enable_cookies), getToggleState(SettingsUtils.isEnableCookies()), TYPE_TOGGLE, "enable_cookies"));
        items.add(new SettingItem(context.getString(R.string.settings_block_images), getToggleState(SettingsUtils.isBlockImages()), TYPE_TOGGLE, "block_images"));
        items.add(new SettingItem(context.getString(R.string.settings_clear_cache), "", TYPE_CLEAR_CACHE, null));
        items.add(new SettingItem(context.getString(R.string.settings_clear_history), "", TYPE_CLEAR_HISTORY, null));
        items.add(new SettingItem(context.getString(R.string.settings_clear_cookies), "", TYPE_CLEAR_COOKIES, null));
        items.add(new SettingItem(context.getString(R.string.settings_clear_all), "", TYPE_CLEAR_ALL, null));
        items.add(new SettingItem(context.getString(R.string.settings_about), "", TYPE_ABOUT, null));
        notifyDataSetChanged();
    }

    private String getSearchEngineName() {
        String[] names = SettingsUtils.getSearchEngineNames();
        int engine = SettingsUtils.getSearchEngine();
        return engine >= 0 && engine < names.length ? names[engine] : "";
    }

    private String getUserAgentName() {
        int mode = SettingsUtils.getUaMode();
        switch (mode) {
            case SettingsUtils.UA_PC:
                return context.getString(R.string.ua_pc);
            case SettingsUtils.UA_CUSTOM:
                return context.getString(R.string.ua_custom);
            default:
                return context.getString(R.string.ua_mobile);
        }
    }

    private String getLanguageName() {
        int lang = SettingsUtils.getLanguage();
        switch (lang) {
            case SettingsUtils.LANG_ENGLISH:
                return context.getString(R.string.language_english);
            case SettingsUtils.LANG_CHINESE:
                return context.getString(R.string.language_chinese);
            default:
                return context.getString(R.string.language_auto);
        }
    }

    private String getToggleState(boolean enabled) {
        return enabled ? context.getString(R.string.state_on) : context.getString(R.string.state_off);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public SettingItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.setting_title);
            holder.subtitle = convertView.findViewById(R.id.setting_subtitle);
            holder.indicator = convertView.findViewById(R.id.setting_indicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SettingItem item = items.get(position);
        holder.title.setText(item.title);

        if (item.subtitle != null && !item.subtitle.isEmpty()) {
            holder.subtitle.setText(item.subtitle);
            holder.subtitle.setVisibility(View.VISIBLE);
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }

        if (item.type == TYPE_TOGGLE) {
            holder.indicator.setVisibility(View.VISIBLE);
            boolean isOn = item.subtitle.equals(context.getString(R.string.state_on));
            holder.indicator.setText(item.subtitle);
            holder.indicator.setBackgroundResource(isOn ? R.drawable.toggle_on_bg : R.drawable.toggle_off_bg);
            holder.indicator.setTextColor(isOn ? 0xFF2d6a4f : 0xFF9E9E9E);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }

        // Animate item
        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeIn.setStartOffset(position * 30);
        convertView.startAnimation(fadeIn);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView subtitle;
        TextView indicator;
    }
}
