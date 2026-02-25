package com.thermostat.utils;

/**
 * Centralizes all configurable values.
 * Override APP_BASE_URL via Maven: mvn test -Dapp.base.url=http://localhost:4000
 */
public class Config {

    public static final String APP_BASE_URL =
            System.getProperty("app.base.url", "http://localhost:5000");

    public static final String APPIUM_SERVER_URL =
            System.getProperty("appium.server.url", "http://127.0.0.1:4723");

    /** Max seconds to wait for an element to appear */
    public static final int EXPLICIT_WAIT_SECONDS = 15;

    /** Pause (ms) after a UI action before asserting — lets React re-render */
    public static final int UI_SETTLE_MS = 600;

    /** Debounce time (ms) the app uses before calling the API after +/- clicks */
    public static final int API_DEBOUNCE_MS = 1000;
}
