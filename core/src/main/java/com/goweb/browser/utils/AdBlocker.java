package com.goweb.browser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.goweb.browser.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdBlocker {

    private static final String TAG = "AdBlocker";
    private static final String PREFS_NAME = "adblock_prefs";
    private static final String KEY_ENABLED = "adblock_enabled";
    private static final String KEY_BLOCK_COUNT = "block_count";
    private static final String KEY_CUSTOM_RULES = "custom_rules";
    private static final String KEY_LAST_UPDATE = "last_update";

    private static AdBlocker instance;
    private final Set<String> blockedDomains = new HashSet<>();
    private final List<Pattern> blockedPatterns = new ArrayList<>();
    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();
    private boolean enabled = true;
    private long blockCount = 0;

    private static final String[] DEFAULT_BLOCKED_DOMAINS = {
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "google-analytics.com",
            "googletagmanager.com",
            "facebook.com/tr",
            "connect.facebook.net",
            "analytics.twitter.com",
            "ads.twitter.com",
            "ads.yahoo.com",
            "ad.yieldmanager.com",
            "ad.doubleclick.net",
            "adserver.adtechus.com",
            "advertising.com",
            "adnxs.com",
            "adsrvr.org",
            "adroll.com",
            "criteo.com",
            "criteo.net",
            "outbrain.com",
            "taboola.com",
            "mgid.com",
            "revcontent.com",
            "zergnet.com",
            "pushengage.com",
            "onesignal.com",
            "pushwoosh.com",
            "urbanairship.com",
            "pagead2.googlesyndication.com",
            "tpc.googlesyndication.com",
            "adclick.g.doubleclick.net",
            "stats.g.doubleclick.net",
            "cm.g.doubleclick.net",
            "adservice.google.com",
            "adservice.google.ca",
            "adservice.google.co.uk",
            "adservice.google.de",
            "adservice.google.fr",
            "adservice.google.co.jp",
            "adservice.google.com.au",
            "adservice.google.com.br",
            "adservice.google.com.hk",
            "adservice.google.com.sg",
            "adservice.google.com.tw",
            "adservice.google.com.mx",
            "adservice.google.com.in",
            "adservice.google.com.kr",
            "moatads.com",
            "scorecardresearch.com",
            "quantserve.com",
            "chartbeat.com",
            "hotjar.com",
            "mixpanel.com",
            "amplitude.com",
            "segment.io",
            "segment.com",
            "optimizely.com",
            "abtest.google.com",
            "cdn.mxpnl.com",
            "cdn.segment.com",
            "cdn.amplitude.com",
            "static.hotjar.com",
            "script.hotjar.com",
            "cdn.mxpnl.com",
            "s3.amazonaws.com/cdn",
            "pixel.facebook.com",
            "pixel.wp.com",
            "pixel.quantserve.com",
            "pixel.scorecardresearch.com",
            "b.scorecardresearch.com",
            "sb.scorecardresearch.com",
            "a.quantserve.com",
            "rules.quantcount.com",
            "secure.quantserve.com",
            "cdn.mxpnl.com",
            "api.mixpanel.com",
            "dec.mixpanel.com",
            "cdn4.mxpnl.com",
            "cdn2.mxpnl.com",
            "ads.stickyadstv.com",
            "ads.pubmatic.com",
            "ads.rubiconproject.com",
            "ads.openx.net",
            "ads.indexww.com",
            "ads.undertone.com",
            "ads.criteo.com",
            "bid.criteo.com",
            "cas.criteo.com",
            "static.criteo.net",
            "img.criteo.net",
            "dis.criteo.com",
            "cat.criteo.com",
            "call.nexage.com",
            "call.pubmatic.com",
            "call.rubiconproject.com",
            "call.openx.net",
            "hbopenbid.pubmatic.com",
            "fastlane.rubiconproject.com",
            "rtb.openx.net",
            "prebid.adnxs.com",
            "ib.adnxs.com",
            "acdn.adnxs.com",
            "cdn.adnxs.com",
            "mediav.com",
            "adm.baidu.com",
            "pos.baidu.com",
            "cbjs.baidu.com",
            "cpro.baidu.com",
            "eclick.baidu.com",
            "hm.baidu.com",
            "nsclick.baidu.com",
            "spcode.baidu.com",
            "tongji.baidu.com",
            "ugc.cdn.baidu.com",
            "bes.baidu.com",
            "drd.baidu.com",
            "js.tanx.com",
            "cdn.tanx.com",
            "e.cn.miaozhen.com",
            "g.cn.miaozhen.com",
            "v2.cn.miaozhen.com",
            "ads.unity3d.com",
            "ads.unitychina.cn",
            "config.unityads.unity3d.com",
            "cdp.cloud.unity3d.com",
            "data-optout-service.uca.cloud.unity3d.com",
            "adskeeper.co.uk",
            "adskeeper.com",
            "blockads.fivefilters.org",
            "pagead.l.google.com",
            "partner.googleadservices.com",
            "afcdn.com",
            "umeng.com",
            "umeng.co",
            "umengcloud.com",
            "alog.umeng.com",
            "alog.umengcloud.com",
            "plbslog.umeng.com",
            "ulog.umeng.com",
            "ulog.umengcloud.com",
            "ads.umeng.com",
            "ads.umengcloud.com",
            "cnzz.com",
            "w.cnzz.com",
            "c.cnzz.com",
            "s22.cnzz.com",
            "s4.cnzz.com",
            "s5.cnzz.com",
            "s9.cnzz.com",
            "s13.cnzz.com",
            "s23.cnzz.com",
            "s9.cnzz.com",
            "tongji.cnzz.com",
            "jpush.cn",
            "jpush.io",
            "jiguang.cn",
            "cdn.jiguang.cn",
            "api.jpush.cn",
            "api.jiguang.cn",
            "getui.net",
            "sdk.open.getui.net",
            "sdk.getui.net",
            "stat.getui.net",
            "log.getui.net",
            "mipcdn.com",
            "c.mipcdn.com",
            "mipcache.bdstatic.com",
            "baichuan.baidu.com",
            "bcfeedback.baidu.com",
            "bceapp.com",
            "bcebos.com",
            "bcecdn.com",
            "bdimg.com",
            "bdstatic.com",
            "bdydns.com",
            "bcebos.com",
            "baidustatic.com",
            "bdurl.net",
            "baiducdn.com",
            "baidubce.com",
            "bcebos.com",
            "smartadserver.com",
            "sascdn.com",
            "ak.sascdn.com",
            "ced.sascdn.com",
            "mobile.sascdn.com",
            "ad.serving-sys.com",
            "serving-sys.com",
            "bs.serving-sys.com",
            "secure.serving-sys.com",
            "a.serving-sys.com",
            "adform.net",
            "track.adform.net",
            "adx.adform.net",
            "adx1.adform.net",
            "s1.adform.net",
            "s2.adform.net",
            "c1.adform.net",
            "c2.adform.net",
            "idsync.adform.net",
            "cm.adform.net",
            "dmp.adform.net",
            "adx.adform.net",
            "banmanpro.com",
            "burstnet.com",
            "casalemedia.com",
            "chartbeat.net",
            "connexity.net",
            "conversantmedia.com",
            "crwdcntrl.net",
            "demdex.net",
            "exelator.com",
            "eyeota.net",
            "flashtalking.com",
            "krxd.net",
            "legolas-media.com",
            "lijit.com",
            "lotame.com",
            "mookie1.com",
            "moatpixel.com",
            "navdmp.com",
            "openx.net",
            "openx.org",
            "optnmstr.com",
            "pippio.com",
            "rlcdn.com",
            "rubiconproject.com",
            "sail-horizon.com",
            "semasio.net",
            "sharethis.com",
            "simpli.fi",
            "sonobi.com",
            "spotxchange.com",
            "srvmath.com",
            "stickyadstv.com",
            "tapad.com",
            "teads.tv",
            "thebrighttag.com",
            "turn.com",
            "underton.com",
            "valueclick.com",
            "viglink.com",
            "w55c.net",
            "xgraph.net",
            "xgraph.net",
            "yieldmo.com",
            "yldbt.com",
            "zemanta.com"
    };

    private static final String[] DEFAULT_BLOCKED_PATTERNS = {
            "/ads[0-9]*\\.",
            "/ad[sx]?[0-9]*\\.",
            "/advert[0-9]*\\.",
            "/banner[s]?[0-9]*\\.",
            "/popup[0-9]*\\.",
            "/popunder[0-9]*\\.",
            "/tracking[0-9]*\\.",
            "/tracker[0-9]*\\.",
            "/analytics[0-9]*\\.",
            "/pixel[0-9]*\\.",
            "/beacon[0-9]*\\.",
            "/telemetry[0-9]*\\.",
            "/stat[s]?[0-9]*\\.",
            "/counter[0-9]*\\.",
            "/collect[0-9]*\\.",
            "/log[s]?[0-9]*\\.",
            "/monitor[0-9]*\\.",
            "/track[0-9]*\\.",
            "doubleclick\\.net/",
            "googlesyndication\\.com/",
            "googleadservices\\.com/",
            "google-analytics\\.com/",
            "googletagmanager\\.com/",
            "pagead2\\.googlesyndication\\.com/",
            "tpc\\.googlesyndication\\.com/",
            "ad\\.doubleclick\\.net/",
            "stats\\.g\\.doubleclick\\.net/",
            "adservice\\.google\\.",
            "moatads\\.com/",
            "scorecardresearch\\.com/",
            "quantserve\\.com/",
            "chartbeat\\.com/",
            "hotjar\\.com/",
            "mixpanel\\.com/",
            "amplitude\\.com/",
            "segment\\.(io|com)/",
            "optimizely\\.com/",
            "ads\\.pubmatic\\.com/",
            "ads\\.rubiconproject\\.com/",
            "ads\\.openx\\.net/",
            "bid\\.criteo\\.com/",
            "cas\\.criteo\\.com/",
            "ib\\.adnxs\\.com/",
            "cdn\\.adnxs\\.com/",
            "hm\\.baidu\\.com/",
            "pos\\.baidu\\.com/",
            "cbjs\\.baidu\\.com/",
            "cpro\\.baidu\\.com/",
            "eclick\\.baidu\\.com/",
            "nsclick\\.baidu\\.com/",
            "tongji\\.baidu\\.com/",
            "umeng\\.com/",
            "cnzz\\.com/",
            "jpush\\.cn/",
            "jiguang\\.cn/",
            "getui\\.net/",
            "/\\.gif\\?[^/]*(utm_|track|pixel|beacon|collect|log)",
            "/1x1\\.",
            "/1x1\\?",
            "/spacer\\.",
            "/clear\\.",
            "/blank\\.",
            "/empty\\.",
            "adservice",
            "ad_frame",
            "ad_wrapper",
            "ad_container",
            "ad_banner",
            "ad_slot",
            "ad_unit",
            "ad_placement",
            "ad_position",
            "ad_zone",
            "ad_space",
            "ad_area",
            "ad_block",
            "ad_box",
            "ad_holder",
            "ad_leaderboard",
            "ad_rectangle",
            "ad_skyscraper",
            "ad_strip",
            "ad_text",
            "ad_title",
            "ad_url",
            "ad_link",
            "ad_iframe",
            "ad_img",
            "ad_image",
            "ad_creative",
            "ad_content",
            "ad_feed",
            "ad_item",
            "ad_list",
            "ad_row",
            "ad_col",
            "ad_section",
            "ad_group",
            "ad_wrapper",
            "ad_overlay",
            "ad_popup",
            "ad_popunder",
            "ad_interstitial",
            "ad_pre_roll",
            "ad_mid_roll",
            "ad_post_roll",
            "ad_video",
            "ad_audio",
            "ad_native",
            "ad_rich",
            "ad_expandable",
            "ad_floating",
            "ad_sticky",
            "ad_footer",
            "ad_header",
            "ad_sidebar",
            "ad_widget",
            "ad_module",
            "ad_component",
            "ad_element",
            "ad_layer",
            "ad_panel",
            "ad_tile",
            "ad_spot",
            "ad_region",
            "ad_location",
            "ad_placement",
            "ad_inventory",
            "ad_serving",
            "ad_delivery",
            "ad_request",
            "ad_response",
            "ad_callback",
            "ad_tracking",
            "ad_reporting",
            "ad_analytics",
            "ad_measurement",
            "ad_verification",
            "ad_viewability",
            "ad_impression",
            "ad_click",
            "ad_conversion",
            "ad_performance",
            "ad_optimization",
            "ad_targeting",
            "ad_retargeting",
            "ad_remessaging",
            "ad_frequency",
            "ad_capping",
            "ad_rotation",
            "ad_weight",
            "ad_priority",
            "ad_bid",
            "ad_auction",
            "ad_exchange",
            "ad_network",
            "ad_partner",
            "ad_publisher",
            "ad_advertiser",
            "ad_agency",
            "ad_campaign",
            "ad_creative",
            "ad_flight",
            "ad_schedule",
            "ad_budget",
            "ad_revenue",
            "ad_ecpm",
            "ad_cpc",
            "ad_cpa",
            "ad_cpl",
            "ad_cps",
            "ad_roi",
            "ad_earnings",
            "ad_payout",
            "ad_commission"
    };

    private static final String[] CSS_SELECTORS = {
            "[class*='ad-banner']",
            "[class*='ad-container']",
            "[class*='ad-wrapper']",
            "[class*='ad-slot']",
            "[class*='ad-unit']",
            "[class*='ad-placement']",
            "[class*='ad-rectangle']",
            "[class*='ad-leaderboard']",
            "[class*='ad-skyscraper']",
            "[class*='ad-strip']",
            "[class*='ad-overlay']",
            "[class*='ad-popup']",
            "[class*='ad-interstitial']",
            "[class*='ad-floating']",
            "[class*='ad-sticky']",
            "[class*='ad-footer']",
            "[class*='ad-header']",
            "[class*='ad-sidebar']",
            "[class*='ad-widget']",
            "[class*='ad-module']",
            "[class*='sponsored']",
            "[class*='promoted']",
            "[class*='native-ad']",
            "[class*='commercial']",
            "[class*='advertisement']",
            "[id*='ad-banner']",
            "[id*='ad-container']",
            "[id*='ad-wrapper']",
            "[id*='ad-slot']",
            "[id*='ad-unit']",
            "[id*='ad-placement']",
            "[id*='google_ads']",
            "[id*='AdSlot']",
            "[id*='div-gpt-ad']",
            "[id*='advertisement']",
            "[id*='sponsored']",
            "[id*='promoted']",
            "ins.adsbygoogle",
            "div[data-ad]",
            "div[data-ad-slot]",
            "div[data-ad-unit]",
            "div[data-ad-client]",
            "iframe[src*='doubleclick']",
            "iframe[src*='googlesyndication']",
            "iframe[src*='adservice']",
            "iframe[src*='adserver']",
            "iframe[src*='advertising']",
            "iframe[src*='ad.']",
            "iframe[src*='/ads/']",
            "iframe[src*='/ad/']"
    };

    private AdBlocker() {
        loadDefaultRules();
        loadPreferences();
    }

    public static synchronized AdBlocker getInstance() {
        if (instance == null) {
            instance = new AdBlocker();
        }
        return instance;
    }

    private void loadDefaultRules() {
        Collections.addAll(blockedDomains, DEFAULT_BLOCKED_DOMAINS);
        for (String patternStr : DEFAULT_BLOCKED_PATTERNS) {
            try {
                blockedPatterns.add(Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                Log.w(TAG, "Invalid pattern: " + patternStr);
            }
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = App.getContext().getSharedPreferences(PREFS_NAME, 0);
        enabled = prefs.getBoolean(KEY_ENABLED, true);
        blockCount = prefs.getLong(KEY_BLOCK_COUNT, 0);
        String customRules = prefs.getString(KEY_CUSTOM_RULES, "");
        if (!customRules.isEmpty()) {
            String[] rules = customRules.split("\n");
            for (String rule : rules) {
                rule = rule.trim();
                if (rule.isEmpty() || rule.startsWith("!") || rule.startsWith("#")) continue;
                if (rule.startsWith("||") && rule.endsWith("^")) {
                    String domain = rule.substring(2, rule.length() - 1);
                    blockedDomains.add(domain);
                } else if (rule.startsWith("/") && rule.endsWith("/")) {
                    try {
                        blockedPatterns.add(Pattern.compile(rule.substring(1, rule.length() - 1), Pattern.CASE_INSENSITIVE));
                    } catch (Exception e) {
                    }
                } else {
                    blockedDomains.add(rule);
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        App.getContext().getSharedPreferences(PREFS_NAME, 0)
                .edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public boolean shouldBlock(String url) {
        if (!enabled) return false;
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("data:") || url.startsWith("blob:") || url.startsWith("javascript:")) return false;
        if (url.startsWith("file:///")) return false;

        Boolean cached = cache.get(url);
        if (cached != null) return cached;

        boolean blocked = checkBlocked(url);
        cache.put(url, blocked);
        if (cache.size() > 2000) {
            cache.clear();
        }
        if (blocked) {
            blockCount++;
            App.getContext().getSharedPreferences(PREFS_NAME, 0)
                    .edit().putLong(KEY_BLOCK_COUNT, blockCount).apply();
        }
        return blocked;
    }

    private boolean checkBlocked(String url) {
        try {
            String host = extractHost(url);
            if (host == null || host.isEmpty()) return false;

            if (blockedDomains.contains(host)) return true;

            for (String domain : blockedDomains) {
                if (host.endsWith("." + domain)) return true;
            }

            for (Pattern pattern : blockedPatterns) {
                if (pattern.matcher(url).find()) return true;
            }

            String path = extractPath(url);
            if (path != null && !path.isEmpty()) {
                String lowerPath = path.toLowerCase(Locale.US);
                if (lowerPath.contains("/ads/") || lowerPath.contains("/ad/") ||
                        lowerPath.contains("/banner/") || lowerPath.contains("/banners/") ||
                        lowerPath.contains("/popup/") || lowerPath.contains("/popunder/") ||
                        lowerPath.contains("/tracking/") || lowerPath.contains("/tracker/") ||
                        lowerPath.contains("/analytics/") || lowerPath.contains("/pixel/") ||
                        lowerPath.contains("/beacon/") || lowerPath.contains("/telemetry/") ||
                        lowerPath.contains("/collect/") || lowerPath.contains("/counter/") ||
                        lowerPath.contains("/advert/") || lowerPath.contains("/advertising/")) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private String extractHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null && host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (URISyntaxException e) {
            int start = url.indexOf("://");
            if (start >= 0) {
                String noScheme = url.substring(start + 3);
                int slash = noScheme.indexOf('/');
                String authority = slash >= 0 ? noScheme.substring(0, slash) : noScheme;
                int colon = authority.indexOf(':');
                return colon >= 0 ? authority.substring(0, colon) : authority;
            }
            return "";
        }
    }

    private String extractPath(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPath();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    public String getCssRules() {
        StringBuilder sb = new StringBuilder();
        sb.append("javascript:(function(){");
        sb.append("var style=document.createElement('style');");
        sb.append("style.type='text/css';");
        sb.append("style.id='goweb-adblock';");
        sb.append("if(document.getElementById('goweb-adblock'))return;");
        sb.append("var css='");
        for (String selector : CSS_SELECTORS) {
            sb.append(selector).append("{display:none!important;visibility:hidden!important;height:0!important;min-height:0!important;overflow:hidden!important;}");
        }
        sb.append("';");
        sb.append("style.innerHTML=css;");
        sb.append("document.head.appendChild(style);");
        sb.append("})()");
        return sb.toString();
    }

    public long getBlockCount() {
        return blockCount;
    }

    public void resetBlockCount() {
        blockCount = 0;
        App.getContext().getSharedPreferences(PREFS_NAME, 0)
                .edit().putLong(KEY_BLOCK_COUNT, 0).apply();
    }

    public void addCustomRule(String rule) {
        if (rule == null || rule.trim().isEmpty()) return;
        rule = rule.trim();
        SharedPreferences prefs = App.getContext().getSharedPreferences(PREFS_NAME, 0);
        String existing = prefs.getString(KEY_CUSTOM_RULES, "");
        String updated = existing.isEmpty() ? rule : existing + "\n" + rule;
        prefs.edit().putString(KEY_CUSTOM_RULES, updated).apply();
        cache.clear();
        if (rule.startsWith("||") && rule.endsWith("^")) {
            blockedDomains.add(rule.substring(2, rule.length() - 1));
        } else if (rule.startsWith("/") && rule.endsWith("/")) {
            try {
                blockedPatterns.add(Pattern.compile(rule.substring(1, rule.length() - 1), Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
            }
        } else {
            blockedDomains.add(rule);
        }
    }

    public void removeCustomRule(String rule) {
        if (rule == null || rule.trim().isEmpty()) return;
        SharedPreferences prefs = App.getContext().getSharedPreferences(PREFS_NAME, 0);
        String existing = prefs.getString(KEY_CUSTOM_RULES, "");
        String updated = existing.replace(rule, "").replace("\n\n", "\n").trim();
        prefs.edit().putString(KEY_CUSTOM_RULES, updated).apply();
        cache.clear();
    }

    public List<String> getCustomRules() {
        SharedPreferences prefs = App.getContext().getSharedPreferences(PREFS_NAME, 0);
        String rules = prefs.getString(KEY_CUSTOM_RULES, "");
        List<String> list = new ArrayList<>();
        if (rules.isEmpty()) return list;
        String[] lines = rules.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) list.add(line);
        }
        return list;
    }

    public void clearCache() {
        cache.clear();
    }

    public int getDomainCount() {
        return blockedDomains.size();
    }

    public int getPatternCount() {
        return blockedPatterns.size();
    }

    public String getStatsText() {
        return String.format(Locale.US, "Blocked: %d ads | Rules: %d domains, %d patterns",
                blockCount, blockedDomains.size(), blockedPatterns.size());
    }
}
