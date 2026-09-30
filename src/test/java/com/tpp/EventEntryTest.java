package com.tpp;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requires the app running locally on http://localhost:8080
 * and a matching ChromeDriver on PATH.
 */
public class EventEntryTest {

    WebDriver driver;
    // static final String BASE_URL = "http://localhost:8080";
    static final String BASE_URL =
        System.getProperty("baseUrl", "http://localhost:8080");
        
    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // uncomment for CI/Jenkins
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(5));
    }

    @Test
    void testSubmitValidEvent() {
        try {
            driver.get(BASE_URL + "/events/new");
            driver.findElement(By.id("species")).sendKeys("Neem");
            driver.findElement(By.id("planterName")).sendKeys("Ravi Kumar");
            driver.findElement(By.id("eventDate")).sendKeys("09/01/2026");
            driver.findElement(By.id("submitBtn")).click();

            WebElement heading = driver.findElement(By.tagName("h2"));
            assertTrue(heading.getText().toLowerCase().contains("success"));
        } catch (AssertionError | Exception e) {
            captureScreenshot("testSubmitValidEvent_failure");
            throw e;
        }
    }

    @Test
    void testDashboardSearch() {
        try {
            driver.get(BASE_URL + "/dashboard?species=Neem");
            assertTrue(driver.getPageSource().contains("Neem") || driver.getPageSource().contains("No records"));
        } catch (AssertionError | Exception e) {
            captureScreenshot("testDashboardSearch_failure");
            throw e;
        }
    }

    @Test
    void testAlertsPageLoads() {
        driver.get(BASE_URL + "/alerts");
        assertTrue(driver.getTitle().contains("Alerts"));
    }

    void captureScreenshot(String testName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.createDirectories(Paths.get("target/failure-screenshots"));
            Files.copy(src.toPath(), Paths.get("target/failure-screenshots/" + testName + ".png"));
        } catch (Exception ignored) {
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }
}
