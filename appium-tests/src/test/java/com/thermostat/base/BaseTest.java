package com.thermostat.base;

import com.thermostat.utils.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * BaseTest sets up and tears down the browser session for every test method.
 *
 * HOW IT WORKS:
 *  - Connects to a local Appium server (default: http://127.0.0.1:4723)
 *  - Launches Chrome in the Appium-managed session (no mobile emulation needed)
 *  - Navigates to the thermostat app URL before each test
 *  - Quits the browser after each test to keep tests independent
 *
 * PREREQUISITES (run once on your machine):
 *  1. npm install -g appium
 *  2. appium driver install chromium   (or 'chrome' for desktop Chrome)
 *  3. appium &                          (start the Appium server)
 *  4. mvn test                          (run all tests)
 *
 * To point at a different app URL:
 *  mvn test -Dapp.base.url=http://localhost:4000
 */
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected RemoteWebDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        log.info("Starting browser session → {}", Config.APP_BASE_URL);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        // Comment the next line out if you want to watch the browser:
        // chromeOptions.addArguments("--headless=new");

        // Appium uses the W3C WebDriver protocol — ChromeOptions work transparently
        driver = new RemoteWebDriver(new URL(Config.APPIUM_SERVER_URL + "/wd/hub"), chromeOptions);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Config.EXPLICIT_WAIT_SECONDS));
        driver.manage().window().maximize();
        driver.get(Config.APP_BASE_URL);
        log.info("Browser launched and navigated to app");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            log.info("Browser session closed");
        }
    }

    /** Pause to allow React to finish re-rendering after a UI action. */
    protected void waitForUiSettle() {
        sleep(Config.UI_SETTLE_MS);
    }

    /** Pause long enough for the API debounce + network round-trip to complete. */
    protected void waitForApiRoundTrip() {
        sleep(Config.API_DEBOUNCE_MS + 500);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
