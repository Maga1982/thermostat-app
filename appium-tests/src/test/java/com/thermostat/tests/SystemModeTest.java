package com.thermostat.tests;

import com.thermostat.base.BaseTest;
import com.thermostat.pages.DashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * SystemModeTest
 *
 * Tests the four system mode buttons: Heat, Cool, Auto, Off.
 *
 * WHAT WE'RE TESTING:
 *  - Each mode button changes the active mode label in the ring
 *  - Switching to 'Off' hides the +/- buttons and slider
 *  - Switching away from 'Off' shows the +/- buttons and slider again
 *  - Rapidly switching modes settles on the last one clicked
 */
public class SystemModeTest extends BaseTest {

    @Test(description = "Clicking 'Heat' mode button activates heat mode")
    public void testSwitchToHeat() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("heat");
        waitForUiSettle();

        Assert.assertEquals(page.getSystemModeLabel(), "heat",
                "System mode label should show 'heat' after clicking Heat");
    }

    @Test(description = "Clicking 'Cool' mode button activates cool mode")
    public void testSwitchToCool() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("cool");
        waitForUiSettle();

        Assert.assertEquals(page.getSystemModeLabel(), "cool",
                "System mode label should show 'cool' after clicking Cool");
    }

    @Test(description = "Clicking 'Auto' mode button activates auto mode")
    public void testSwitchToAuto() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("auto");
        waitForUiSettle();

        Assert.assertEquals(page.getSystemModeLabel(), "auto",
                "System mode label should show 'auto' after clicking Auto");
    }

    @Test(description = "Clicking 'Off' mode button activates off mode")
    public void testSwitchToOff() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("off");
        waitForUiSettle();

        Assert.assertEquals(page.getSystemModeLabel(), "off",
                "System mode label should show 'off' after clicking Off");
    }

    @Test(description = "Switching to 'Off' hides the temperature +/- buttons and slider")
    public void testOffModeHidesControls() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("off");
        waitForUiSettle();

        Assert.assertFalse(page.isIncreaseButtonVisible(),
                "+ button should NOT be visible when system is Off");
        Assert.assertFalse(page.isDecreaseButtonVisible(),
                "− button should NOT be visible when system is Off");
        Assert.assertFalse(page.isSliderVisible(),
                "Slider should NOT be visible when system is Off");
    }

    @Test(description = "Switching from 'Off' to 'Heat' restores temperature controls")
    public void testSwitchingFromOffRestoresControls() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        // First go to Off
        page.clickSystemMode("off");
        waitForUiSettle();
        Assert.assertFalse(page.isIncreaseButtonVisible(), "Controls should be hidden in Off mode");

        // Then switch to Heat
        page.clickSystemMode("heat");
        waitForUiSettle();

        Assert.assertTrue(page.isIncreaseButtonVisible(),
                "+ button should reappear after switching from Off to Heat");
        Assert.assertTrue(page.isSliderVisible(),
                "Slider should reappear after switching from Off to Heat");
    }

    @Test(description = "Cycling through all four modes works correctly")
    public void testCycleThroughAllModes() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        String[] modes = {"heat", "cool", "auto", "off"};

        for (String mode : modes) {
            page.clickSystemMode(mode);
            waitForUiSettle();

            String displayed = page.getSystemModeLabel();
            Assert.assertEquals(displayed, mode,
                    "After clicking " + mode + ", label should be " + mode + " but was " + displayed);
            log.info("Switched to mode: {} ✓", mode);
        }
    }

    @Test(description = "Rapidly clicking different modes settles on the last one")
    public void testRapidModeSwitching() {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        // Click quickly without waiting
        page.clickSystemMode("heat");
        page.clickSystemMode("cool");
        page.clickSystemMode("auto");
        waitForUiSettle(); // Only wait after all clicks

        Assert.assertEquals(page.getSystemModeLabel(), "auto",
                "After rapid switching ending on auto, mode should be 'auto'");
    }
}
