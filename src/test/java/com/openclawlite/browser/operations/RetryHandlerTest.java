package com.openclawlite.browser.operations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RetryHandler 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RetryHandlerTest {

    @Mock
    private WebDriver mockDriver;

    private RetryHandler retryHandler;

    @BeforeEach
    void setUp() {
        retryHandler = new RetryHandler(mockDriver, 3, 100);
    }

    @Test
    void testSuccessfulOperation() {
        Supplier<String> supplier = () -> "success";

        RetryHandler.RetryResult<String> result = retryHandler.executeWithRetry(supplier);

        assertTrue(result.success());
        assertEquals("success", result.result());
        assertEquals(1, result.attempts());
        assertTrue(result.totalTime() < 1000);
    }

    @Test
    void testRetryOnStaleElementReference() {
        WebElement mockElement = mock(WebElement.class);

        // 第一次失败，第二次成功
        when(mockElement.isDisplayed())
            .thenThrow(new org.openqa.selenium.StaleElementReferenceException("stale"))
            .thenReturn(true);

        Supplier<Boolean> supplier = () -> mockElement.isDisplayed();

        RetryHandler.RetryResult<Boolean> result = retryHandler.executeWithRetry(supplier);

        assertTrue(result.success());
        assertTrue(result.result());
        assertEquals(2, result.attempts());
    }

    @Test
    void testRetryOnNoSuchElement() {
        WebElement mockElement = mock(WebElement.class);

        // 第一次失败，第二次成功
        when(mockElement.getText())
            .thenThrow(new NoSuchElementException("not found"))
            .thenReturn("text");

        Supplier<String> supplier = () -> mockElement.getText();

        RetryHandler.RetryResult<String> result = retryHandler.executeWithRetry(supplier);

        assertTrue(result.success());
        assertEquals("text", result.result());
        assertEquals(2, result.attempts());
    }

    @Test
    void testMaxRetriesExceeded() {
        Supplier<String> supplier = () -> {
            throw new NoSuchElementException("always fails");
        };

        RetryHandler.RetryResult<String> result = retryHandler.executeWithRetry(supplier);

        assertFalse(result.success());
        assertNull(result.result());
        assertEquals(3, result.attempts());
        assertNotNull(result.error());
        assertTrue(result.error().contains("所有重试都失败"));
    }

    @Test
    void testCustomConfig() {
        RetryHandler.RetryConfig config = new RetryHandler.RetryConfig()
            .maxRetries(5)
            .baseDelay(200)
            .backoffFactor(3.0);

        assertEquals(5, config.getMaxRetries());
        assertEquals(200, config.getBaseDelay());
        assertEquals(3.0, config.getBackoffFactor());
    }

    @Test
    void testFailFastConfig() {
        RetryHandler.RetryConfig config = retryHandler.getFailFastConfig();

        assertEquals(1, config.getMaxRetries());
        assertEquals(0, config.getBaseDelay());
        assertFalse(config.isRetryOnNoSuchElement());
        assertFalse(config.isRetryOnStaleElement());
    }

    @Test
    void testAggressiveRetryConfig() {
        RetryHandler.RetryConfig config = retryHandler.getAggressiveRetryConfig();

        assertEquals(10, config.getMaxRetries());
        assertEquals(500, config.getBaseDelay());
        assertEquals(1.5, config.getBackoffFactor());
    }

    @Test
    void testConservativeRetryConfig() {
        RetryHandler.RetryConfig config = retryHandler.getConservativeRetryConfig();

        assertEquals(2, config.getMaxRetries());
        assertEquals(2000, config.getBaseDelay());
        assertEquals(3.0, config.getBackoffFactor());
    }

    @Test
    void testFindElementWithRetry() {
        By selector = By.id("test");
        WebElement mockElement = mock(WebElement.class);

        // 模拟第一次失败，第二次成功
        when(mockDriver.findElement(selector))
            .thenThrow(new NoSuchElementException("not found"))
            .thenReturn(mockElement);

        RetryHandler.RetryResult<WebElement> result = retryHandler.findElementWithRetry(selector);

        assertTrue(result.success());
        assertSame(mockElement, result.result());
    }

    @Test
    void testClickWithRetry() {
        WebElement mockElement = mock(WebElement.class);

        // 第一次失败，第二次成功
        doThrow(new org.openqa.selenium.StaleElementReferenceException("stale"))
            .doNothing()
            .when(mockElement)
            .click();

        RetryHandler.RetryResult<Void> result = retryHandler.clickWithRetry(mockElement);

        assertTrue(result.success());
        verify(mockElement, times(2)).click();
    }

    @Test
    void testSendKeysWithRetry() {
        WebElement mockElement = mock(WebElement.class);

        RetryHandler.RetryResult<Void> result = retryHandler.sendKeysWithRetry(
            mockElement, "test text");

        assertTrue(result.success());
        verify(mockElement).clear();
        verify(mockElement).sendKeys("test text");
    }

    @Test
    void testCalculateDelay() {
        // 测试延迟计算（指数退避）
        long delay1 = 100; // baseDelay
        long delay2 = (long) (100 * Math.pow(2.0, 1)); // baseDelay * backoffFactor^1
        long delay3 = (long) (100 * Math.pow(2.0, 2)); // baseDelay * backoffFactor^2

        assertEquals(200, delay2);
        assertEquals(400, delay3);
    }
}
