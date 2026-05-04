package com.goweb.browser.extension;

public interface TranslateInterface {

    interface TranslateCallback {
        void onSuccess(String translatedText);
        void onError(String errorMessage);
    }

    void translate(String text, String sourceLang, String targetLang, TranslateCallback callback);

    void translatePage(String pageUrl, String targetLang, TranslateCallback callback);

    String[] getSupportedLanguages();

    String getLanguageCode(String languageName);

    void setApiKey(String apiKey);

    boolean isAvailable();
}
