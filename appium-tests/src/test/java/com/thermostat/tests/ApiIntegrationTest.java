package com.thermostat.tests;

import com.thermostat.base.BaseTest;
import com.thermostat.pages.DashboardPage;
import com.thermostat.utils.Config;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * ApiIntegrationTest
 *
 * Verifies that UI actions actually persist to the backend API,
 * not just update the local React state.
 *
 * IMPORTANT — DATABASE SAFETY:
 *  These tests call the real API which writes to the real Firebase database.
 *  To avoid leaving dirty data behind, @BeforeMethod snapshots the current
 *  thermostat state and @AfterMethod always restores it, even if the test fails.
 *  This means the database ends up in exactly the same state it started in.
 *
 * HOW IT WORKS:
 *  1. Snapshot — read current targetTemp + systemMode from the API
 *  2. Open the app in the browser (via Appium/WebDriver)
 *  3. Perform a UI action (e.g. click +)
 *  4. Wait for the debounce + network round-trip to complete
 *  5. Assert the API response reflects the change
 *  6. Restore — PATCH the original values back, regardless of pass/fail
 */
public class ApiIntegrationTest extends BaseTest {

    private final HttpClient http = HttpClient.newHttpClient();

    // Captured before each test, restored after each test
    private int originalTargetTemp;
    private String originalSystemMode;

    // ── Snapshot & Restore ───────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void snapshotState() throws IOException, InterruptedException {
        originalTargetTemp = fetchTargetTempFromApi();
        originalSystemMode = fetchSystemModeFromApi();
        log.info("Snapshot — targetTemp: {}°, systemMode: '{}'", originalTargetTemp, originalSystemMode);
    }

    /**
     * Runs after EVERY test (pass or fail) and patches the API back
     * to the values that existed before the test ran.
     * The database is left exactly as it was found.
     */
    @AfterMethod(alwaysRun = true)
    public void restoreState() throws IOException, InterruptedException {
        String body = "{\"targetTemp\":" + originalTargetTemp
                + ",\"systemMode\":\"" + originalSystemMode + "\"}";

        http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(Config.APP_BASE_URL + "/api/thermostats/1"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        log.info("Restored — targetTemp: {}°, systemMode: '{}'", originalTargetTemp, originalSystemMode);
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test(description = "Clicking + sends the updated target temperature to the API")
    public void testIncreaseTempPersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        if (page.getSystemModeLabel().equals("off")) {
            page.clickSystemMode("heat");
            waitForUiSettle();
        }

        int apiBefore = fetchTargetTempFromApi();
        page.clickIncreaseTemp();
        waitForApiRoundTrip();

        int apiAfter = fetchTargetTempFromApi();
        log.info("targetTemp API: {} → {}", apiBefore, apiAfter);

        Assert.assertEquals(apiAfter, apiBefore + 1,
                "API targetTemp should increase by 1 after clicking + in the UI");
    }

    @Test(description = "Clicking − sends the updated target temperature to the API")
    public void testDecreaseTempPersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        if (page.getSystemModeLabel().equals("off")) {
            page.clickSystemMode("heat");
            waitForUiSettle();
        }

        int apiBefore = fetchTargetTempFromApi();
        page.clickDecreaseTemp();
        waitForApiRoundTrip();

        int apiAfter = fetchTargetTempFromApi();
        log.info("targetTemp API: {} → {}", apiBefore, apiAfter);

        Assert.assertEquals(apiAfter, apiBefore - 1,
                "API targetTemp should decrease by 1 after clicking − in the UI");
    }

    @Test(description = "Switching system mode to 'heat' persists to the API")
    public void testSystemModePersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("heat");
        waitForApiRoundTrip();

        String apiMode = fetchSystemModeFromApi();
        log.info("systemMode API after clicking Heat: '{}'", apiMode);
        Assert.assertEquals(apiMode, "heat",
                "API systemMode should be 'heat' after clicking Heat button");
    }

    @Test(description = "Switching system mode to 'cool' persists to the API")
    public void testCoolModePersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();
        page.clickSystemMode("cool");
        waitForApiRoundTrip();

        String apiMode = fetchSystemModeFromApi();
        Assert.assertEquals(apiMode, "cool",
                "API systemMode should be 'cool' after clicking Cool button");
    }

    @Test(description = "GET /api/thermostats returns HTTP 200 with valid JSON")
    public void testApiHealthCheck() throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(Config.APP_BASE_URL + "/api/thermostats"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        Assert.assertEquals(response.statusCode(), 200,
                "GET /api/thermostats should return HTTP 200");

        String body = response.body();
        Assert.assertTrue(body.contains("currentTemp"), "Response should contain 'currentTemp'");
        Assert.assertTrue(body.contains("targetTemp"),  "Response should contain 'targetTemp'");
        Assert.assertTrue(body.contains("systemMode"),  "Response should contain 'systemMode'");
        log.info("API health check passed. Response: {}", body);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private int fetchTargetTempFromApi() throws IOException, InterruptedException {
        return parseIntField(fetchApiBody(), "targetTemp");
    }

    private String fetchSystemModeFromApi() throws IOException, InterruptedException {
        return parseStringField(fetchApiBody(), "systemMode");
    }

    private String fetchApiBody() throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(Config.APP_BASE_URL + "/api/thermostats"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Assert.assertEquals(response.statusCode(), 200, "API should return 200 OK");
        return response.body();
    }

    private int parseIntField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int idx = json.indexOf(pattern);
        Assert.assertTrue(idx >= 0, "Field '" + field + "' not found in: " + json);
        String rest = json.substring(idx + pattern.length()).trim();
        StringBuilder digits = new StringBuilder();
        for (char c : rest.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else break;
        }
        return Integer.parseInt(digits.toString());
    }

    private String parseStringField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        Assert.assertTrue(start >= 0, "Field '" + field + "' not found in: " + json);
        int valueStart = start + pattern.length();
        int valueEnd = json.indexOf("\"", valueStart);
        return json.substring(valueStart, valueEnd);
    }
}
