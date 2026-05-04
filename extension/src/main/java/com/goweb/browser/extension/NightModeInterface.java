package com.goweb.browser.extension;

public interface NightModeInterface {

    void applyNightMode(boolean enabled);

    void setSchedule(int startHour, int endMinute, int endHour, int startMinute);

    boolean isScheduled();

    void setCustomFilter(int brightness, int colorTemperature);

    String getNightModeCss();

    void setEnabled(boolean enabled);

    boolean isEnabled();
}
