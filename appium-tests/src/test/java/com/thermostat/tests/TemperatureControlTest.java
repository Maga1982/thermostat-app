package com.thermostat.tests;

import com.thermostat.base.BaseTest;
import com.thermostat.pages.DashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TemperatureControlTest
 *
 * Tests the + and − buttons that adjust the target (set-point) temperature.
 *
 * WHAT WE'RE TESTING:
 *  - Clicking + raises the displayed target by 1°
 *  - Clicking − lowers the displayed target by 1°
 *  - Multiple consecutive clicks accumulate correctly
 *  - Temperature cannot go below 50° (minimum clamp)
 *  - Temperature cannot go above 90° (maximum clamp)
 *
 * NOTE: These tests only verify the UI — they do not wait for the API to
 * confirm the change. A separate ApiIntegrationTest handles that scenario.
 */
public class TemperatureControlTest extends BaseTest {

    @Test(description = "Clicking + increases target temperature by 1 degree")
    public void testIncreaseTempByOne() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        int before = page.getTargetTemp();
        page.clickIncreaseTemp();
        waitForUiSettle();
        int after = page.getTargetTemp();

        Assert.assertEquals(after, before + 1,
                "Target temp should increase by 1 after clicking +");
        log.info("Target temp: {} → {}", before, after);
    }

    @Test(description = "Clicking − decreases target temperature by 1 degree")
    public void testDecreaseTempByOne() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        int before = page.getTargetTemp();
        page.clickDecreaseTemp();
        waitForUiSettle();
        int after = page.getTargetTemp();

        Assert.assertEquals(after, before - 1,
                "Target temp should decrease by 1 after clicking −");
        log.info("Target temp: {} → {}", before, after);
    }

    @Test(description = "Clicking + three times increases target temperature by 3 degrees")
    public void testIncreaseByThree() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        int before = page.getTargetTemp();
        page.clickIncreaseTemp();
        page.clickIncreaseTemp();
        page.clickIncreaseTemp();
        waitForUiSettle();
        int after = page.getTargetTemp();

        // Account for max clamp at 90
        int expected = Math.min(before + 3, 90);
        Assert.assertEquals(after, expected,
                "Target temp should increase by 3 (or clamp to 90)");
        log.info("Target temp: {} → {} (expected {})", before, after, expected);
    }

    @Test(description = "Temperature cannot be increased above the maximum of 90°")
    public void testTemperatureMaxClamp() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        // Click + many times to try to exceed 90
        for (int i = 0; i < 50; i++) {
            page.clickIncreaseTemp();
        }
        waitForUiSettle();
        int after = page.getTargetTemp();

        Assert.assertTrue(after <= 90,
                "Target temp should never exceed 90°, but was: " + after);
        log.info("After 50 clicks of +, target temp is: {}°", after);
    }

    @Test(description = "Temperature cannot be decreased below the minimum of 50°")
    public void testTemperatureMinClamp() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        // Click − many times to try to go below 50
        for (int i = 0; i < 50; i++) {
            page.clickDecreaseTemp();
        }
        waitForUiSettle();
        int after = page.getTargetTemp();

        Assert.assertTrue(after >= 50,
                "Target temp should never go below 50°, but was: " + after);
        log.info("After 50 clicks of −, target temp is: {}°", after);
    }

    @Test(description = "Increase then decrease returns to the original temperature")
    public void testIncreaseThenDecrease() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        skipIfOff(page);

        int original = page.getTargetTemp();
        page.clickIncreaseTemp();
        waitForUiSettle();
        page.clickDecreaseTemp();
        waitForUiSettle();
        int result = page.getTargetTemp();

        Assert.assertEquals(result, original,
                "After +1 then −1, target temp should return to original: " + original);
    }

    /**
     * Helper: skip the test if the system is in 'off' mode,
     * because the +/- buttons are not rendered in off mode.
     */
    private void skipIfOff(DashboardPage page) {
        String mode = page.getSystemModeLabel();
        if (mode.equals("off")) {
            log.info("System is OFF — switching to heat for this test");
            page.clickSystemMode("heat");
            waitForUiSettle();
        }
    }
}
