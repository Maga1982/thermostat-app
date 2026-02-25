package com.thermostat.tests;

import com.thermostat.base.BaseTest;
import com.thermostat.pages.DashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * DashboardLoadTest
 *
 * Verifies that the thermostat dashboard loads correctly and displays
 * all expected UI sections. These are "smoke tests" — run them first
 * to confirm the app is reachable before running deeper feature tests.
 */
public class DashboardLoadTest extends BaseTest {

    @Test(description = "Dashboard renders without errors after page load")
    public void testDashboardIsVisible() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        Assert.assertTrue(page.isDashboardVisible(),
                "Dashboard container should be visible after load");
    }

    @Test(description = "Thermostat name is displayed in the header")
    public void testThermostatNameIsDisplayed() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String name = page.getThermostatName();

        Assert.assertNotNull(name, "Thermostat name should not be null");
        Assert.assertFalse(name.isBlank(), "Thermostat name should not be blank");
        log.info("Thermostat name shown: '{}'", name);
    }

    @Test(description = "Online status indicator is shown")
    public void testOnlineStatusShown() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String status = page.getStatusText();
        Assert.assertTrue(status.toLowerCase().contains("online"),
                "Status should show 'Online' but was: " + status);
    }

    @Test(description = "Current (indoor) temperature is displayed")
    public void testCurrentTempDisplayed() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String currentTemp = page.getCurrentTempText();

        Assert.assertNotNull(currentTemp, "Current temperature should not be null");
        Assert.assertTrue(currentTemp.contains("°"),
                "Current temperature should include degree symbol, but was: " + currentTemp);
        log.info("Current temp shown: '{}'", currentTemp);
    }

    @Test(description = "Target temperature is a realistic value between 50 and 90")
    public void testTargetTempIsRealistic() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        int target = page.getTargetTemp();

        Assert.assertTrue(target >= 50 && target <= 90,
                "Target temp should be between 50 and 90, but was: " + target);
        log.info("Target temp shown: {}°", target);
    }

    @Test(description = "Humidity reading is displayed")
    public void testHumidityDisplayed() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String humidity = page.getHumidityText();

        Assert.assertNotNull(humidity, "Humidity should not be null");
        Assert.assertTrue(humidity.contains("%"),
                "Humidity should include '%' symbol, but was: " + humidity);
        log.info("Humidity shown: '{}'", humidity);
    }

    @Test(description = "System mode label is shown in the ring")
    public void testSystemModeLabelVisible() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String mode = page.getSystemModeLabel();

        Assert.assertFalse(mode.isBlank(), "System mode label should not be blank");
        Assert.assertTrue(
                mode.equals("heat") || mode.equals("cool") || mode.equals("auto") || mode.equals("off"),
                "System mode should be one of heat/cool/auto/off, but was: " + mode
        );
        log.info("System mode label shown: '{}'", mode);
    }

    @Test(description = "+/- temperature buttons are visible when system is on")
    public void testTemperatureButtonsVisibleWhenOn() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String mode = page.getSystemModeLabel();

        if (!mode.equals("off")) {
            Assert.assertTrue(page.isIncreaseButtonVisible(), "+ button should be visible when system is on");
            Assert.assertTrue(page.isDecreaseButtonVisible(), "− button should be visible when system is on");
            Assert.assertTrue(page.isSliderVisible(), "Temperature slider should be visible when system is on");
        } else {
            log.info("System is OFF — skipping button visibility check");
        }
    }
}
