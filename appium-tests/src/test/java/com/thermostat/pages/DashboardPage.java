package com.thermostat.pages;

import com.thermostat.utils.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the thermostat Dashboard.
 *
 * All element locators live here. Tests never contain raw selectors —
 * they call methods on this class instead. This means if the UI changes,
 * you only update one file.
 *
 * Locator strategy: data-testid attributes are preferred because they are
 * stable (not tied to CSS class names or element order).
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ────────────────────────────────────────────────────────────
    private static final By DASHBOARD           = By.cssSelector("[data-testid='dashboard']");
    private static final By THERMOSTAT_NAME     = By.cssSelector("[data-testid='text-thermostat-name']");
    private static final By STATUS_ONLINE       = By.cssSelector("[data-testid='status-online']");
    private static final By CURRENT_TEMP        = By.cssSelector("[data-testid='text-current-temp']");
    private static final By TARGET_TEMP         = By.cssSelector("[data-testid='text-target-temp']");
    private static final By SYSTEM_MODE_LABEL   = By.cssSelector("[data-testid='text-system-mode']");
    private static final By HUMIDITY            = By.cssSelector("[data-testid='text-humidity']");
    private static final By BTN_INCREASE        = By.cssSelector("[data-testid='button-increase-temp']");
    private static final By BTN_DECREASE        = By.cssSelector("[data-testid='button-decrease-temp']");
    private static final By SLIDER              = By.cssSelector("[data-testid='input-temp-slider']");
    private static final By SYSTEM_MODE_GROUP   = By.cssSelector("[data-testid='control-system-mode']");
    private static final By FAN_MODE_GROUP      = By.cssSelector("[data-testid='control-fan-mode']");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.EXPLICIT_WAIT_SECONDS));
    }

    // ── Waits ────────────────────────────────────────────────────────────────

    /** Block until the dashboard is fully rendered (thermostat name visible). */
    public DashboardPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(THERMOSTAT_NAME));
        return this;
    }

    // ── Read state ───────────────────────────────────────────────────────────

    public boolean isDashboardVisible() {
        return !driver.findElements(DASHBOARD).isEmpty();
    }

    public String getThermostatName() {
        return driver.findElement(THERMOSTAT_NAME).getText();
    }

    public String getStatusText() {
        return driver.findElement(STATUS_ONLINE).getText().trim();
    }

    /** Returns the current (indoor) temperature string, e.g. "72°" */
    public String getCurrentTempText() {
        return driver.findElement(CURRENT_TEMP).getText();
    }

    /** Returns the target temperature as an integer. */
    public int getTargetTemp() {
        String text = driver.findElement(TARGET_TEMP).getText().trim();
        return Integer.parseInt(text);
    }

    /** Returns the active system mode label, e.g. "cool", "heat". */
    public String getSystemModeLabel() {
        return driver.findElement(SYSTEM_MODE_LABEL).getText().trim().toLowerCase();
    }

    /** Returns the humidity display string, e.g. "45%" */
    public String getHumidityText() {
        return driver.findElement(HUMIDITY).getText();
    }

    public boolean isIncreaseButtonVisible() {
        return !driver.findElements(BTN_INCREASE).isEmpty();
    }

    public boolean isDecreaseButtonVisible() {
        return !driver.findElements(BTN_DECREASE).isEmpty();
    }

    public boolean isSliderVisible() {
        return !driver.findElements(SLIDER).isEmpty();
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    /** Click the + button once to raise target temperature by 1°. */
    public DashboardPage clickIncreaseTemp() {
        driver.findElement(BTN_INCREASE).click();
        return this;
    }

    /** Click the − button once to lower target temperature by 1°. */
    public DashboardPage clickDecreaseTemp() {
        driver.findElement(BTN_DECREASE).click();
        return this;
    }

    /**
     * Click a system mode button by its id: "heat", "cool", "auto", or "off".
     * Example: clickSystemMode("heat")
     */
    public DashboardPage clickSystemMode(String modeId) {
        By locator = By.cssSelector("[data-testid='button-mode-" + modeId + "']");
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        return this;
    }

    /**
     * Click a fan mode button by its id: "auto" or "on".
     * Example: clickFanMode("on")
     */
    public DashboardPage clickFanMode(String modeId) {
        By locator = By.cssSelector("[data-testid='button-fan-" + modeId + "']");
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        return this;
    }
}
