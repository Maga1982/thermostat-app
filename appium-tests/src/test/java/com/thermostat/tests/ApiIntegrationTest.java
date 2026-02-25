package com.thermostat.tests;

import com.thermostat.base.BaseTest;
import com.thermostat.pages.DashboardPage;
import com.thermostat.utils.Config;
import org.testng.Assert;
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
 * HOW IT WORKS:
 *  1. Open the app in the browser (via Appium/WebDriver)
 *  2. Perform a UI action (e.g. click +)
 *  3. Wait for the debounce + network round-trip to complete
 *  4. Call the REST API directly from Java using HttpClient
 *  5. Assert the API response reflects the change
 *
 * This proves the full loop works:
 *   UI click → React state → PATCH /api/thermostats/1 → Firebase → GET response
 */
public class ApiIntegrationTest extends BaseTest {

    private final HttpClient http = HttpClient.newHttpClient();

    @Test(description = "Clicking + sends the updated target temperature to the API")
    public void testIncreaseTempPersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        // Ensure system is on (buttons visible)
        if (page.getSystemModeLabel().equals("off")) {
            page.clickSystemMode("heat");
            waitForUiSettle();
        }

        int uiBefore = page.getTargetTemp();
        int apiBefore = fetchTargetTempFromApi();
        log.info("Before click — UI: {}°, API: {}°", uiBefore, apiBefore);

        page.clickIncreaseTemp();
        waitForApiRoundTrip(); // Wait for debounce + HTTP round-trip

        int apiAfter = fetchTargetTempFromApi();
        log.info("After click — API: {}°", apiAfter);

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
        log.info("Decrease: API {} → {}", apiBefore, apiAfter);

        Assert.assertEquals(apiAfter, apiBefore - 1,
                "API targetTemp should decrease by 1 after clicking − in the UI");
    }

    @Test(description = "Switching system mode to 'heat' persists to the API")
    public void testSystemModePersistsToApi() throws IOException, InterruptedException {
        DashboardPage page = new DashboardPage(driver).waitUntilLoaded();

        page.clickSystemMode("heat");
        waitForApiRoundTrip();

        String apiMode = fetchSystemModeFromApi();
        log.info("API systemMode after clicking Heat: '{}'", apiMode);

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
        Assert.assertTrue(body.contains("currentTemp"),
                "Response should contain 'currentTemp' field");
        Assert.assertTrue(body.contains("targetTemp"),
                "Response should contain 'targetTemp' field");
        Assert.assertTrue(body.contains("systemMode"),
                "Response should contain 'systemMode' field");

        log.info("API health check passed. Response: {}", body);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Calls GET /api/thermostats and parses the targetTemp value.
     * Uses basic string parsing to avoid needing a JSON library dependency.
     */
    private int fetchTargetTempFromApi() throws IOException, InterruptedException {
        String body = fetchApiBody();
        return parseIntField(body, "targetTemp");
    }

    private String fetchSystemModeFromApi() throws IOException, InterruptedException {
        String body = fetchApiBody();
        return parseStringField(body, "systemMode");
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

    /** Parse a numeric field from a JSON string without a JSON library. */
    private int parseIntField(String json, String field) {
        // Matches: "targetTemp":70  or  "targetTemp": 70
        String pattern = "\"" + field + "\":";
        int idx = json.indexOf(pattern);
        Assert.assertTrue(idx >= 0, "Field '" + field + "' not found in API response: " + json);
        String rest = json.substring(idx + pattern.length()).trim();
        // Read digits until non-digit
        StringBuilder digits = new StringBuilder();
        for (char c : rest.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else break;
        }
        return Integer.parseInt(digits.toString());
    }

    /** Parse a string field from a JSON string without a JSON library. */
    private String parseStringField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        Assert.assertTrue(start >= 0, "Field '" + field + "' not found in API response: " + json);
        int valueStart = start + pattern.length();
        int valueEnd = json.indexOf("\"", valueStart);
        return json.substring(valueStart, valueEnd);
    }
}
