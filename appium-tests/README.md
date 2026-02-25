# Thermostat App — Appium E2E Tests

End-to-end automation tests for the Smart Thermostat web app, built with **Appium + Java + TestNG**.

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java | 17+ | `brew install openjdk@17` |
| Maven | 3.9+ | `brew install maven` |
| Node.js | 18+ | Already installed |
| Appium | 2.x | `npm install -g appium` |
| Appium Chrome Driver | latest | `appium driver install chromium` |
| Google Chrome | any | Already installed |

## Setup (one-time)

```bash
# 1. Install Appium globally
npm install -g appium

# 2. Install the Chrome/Chromium driver for Appium
appium driver install chromium

# 3. Verify Appium is ready
appium driver list
```

## Running the Tests

**Step 1 — Start the thermostat app** (in one terminal):
```bash
# From the project root
npm run dev
# App runs at http://localhost:5000
```

**Step 2 — Start the Appium server** (in another terminal):
```bash
appium
# Appium listens at http://127.0.0.1:4723
```

**Step 3 — Run the tests** (in a third terminal):
```bash
cd appium-tests
mvn test
```

### Run against a different port
```bash
mvn test -Dapp.base.url=http://localhost:4000
```

### Run a single test class
```bash
mvn test -Dtest=DashboardLoadTest
mvn test -Dtest=SystemModeTest
mvn test -Dtest=TemperatureControlTest
mvn test -Dtest=FanModeTest
mvn test -Dtest=ApiIntegrationTest
```

## Project Structure

```
appium-tests/
├── pom.xml                          # Maven config, all dependencies
├── testng.xml                       # Test suite definition (run order)
├── README.md
└── src/test/java/com/thermostat/
    ├── base/
    │   └── BaseTest.java            # Browser setup/teardown (BeforeMethod/AfterMethod)
    ├── pages/
    │   └── DashboardPage.java       # Page Object — all locators & actions live here
    ├── tests/
    │   ├── DashboardLoadTest.java   # Smoke tests: does the page load correctly?
    │   ├── TemperatureControlTest.java  # +/- button behaviour, clamping
    │   ├── SystemModeTest.java      # Heat/Cool/Auto/Off mode switching
    │   ├── FanModeTest.java         # Fan Auto/On switching
    │   └── ApiIntegrationTest.java  # UI actions → persisted to REST API
    └── utils/
        └── Config.java              # All configurable values (URL, timeouts)
```

## Test Suites Explained

| Suite | What it tests |
|-------|--------------|
| `DashboardLoadTest` | Page loads, all sections visible, realistic values shown |
| `TemperatureControlTest` | +/- buttons change target temp; min/max clamping works |
| `SystemModeTest` | Mode buttons change active mode; Off hides/shows controls |
| `FanModeTest` | Fan mode buttons are clickable; visible in all system modes |
| `ApiIntegrationTest` | UI actions actually reach the backend API (full loop test) |

## How Appium Is Used Here

Appium is used as a **WebDriver-compatible server** to drive a Chrome browser.  
The tests use the standard Selenium `RemoteWebDriver` API — no mobile-specific code needed.

```
Test code (Java)
    ↓ RemoteWebDriver protocol (W3C)
Appium server (localhost:4723)
    ↓ ChromeDriver
Chrome browser
    ↓ HTTP
Thermostat App (localhost:5000)
    ↓ HTTP
Firebase / Express API
```

## Selectors Strategy

All element selectors use `data-testid` attributes, e.g.:

```java
By.cssSelector("[data-testid='button-increase-temp']")
```

This makes tests resilient to CSS class changes or DOM restructuring.
The `data-testid` values are defined directly in the React components.
