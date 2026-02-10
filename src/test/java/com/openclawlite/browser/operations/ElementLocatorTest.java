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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ElementLocator 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ElementLocatorTest {

    @Mock
    private WebDriver mockDriver;

    private ElementLocator elementLocator;

    @BeforeEach
    void setUp() {
        elementLocator = new ElementLocator(mockDriver, 10);
    }

    @Test
    void testFindElementByCssSelector() {
        WebElement mockElement = mock(WebElement.class);
        when(mockDriver.findElement(By.cssSelector("#test"))).thenReturn(mockElement);

        WebElement element = elementLocator.findElement("#test", ElementLocator.LocatorStrategy.CSS);

        assertSame(mockElement, element);
    }

    @Test
    void testFindElementByXPath() {
        WebElement mockElement = mock(WebElement.class);
        when(mockDriver.findElement(By.xpath("//div[@id='test']"))).thenReturn(mockElement);

        WebElement element = elementLocator.findElement("//div[@id='test']", ElementLocator.LocatorStrategy.XPATH);

        assertSame(mockElement, element);
    }

    @Test
    void testFindElementByText() {
        WebElement mockElement = mock(WebElement.class);
        when(mockDriver.findElement(By.xpath(".//*[text()='Test Text']"))).thenReturn(mockElement);

        WebElement element = elementLocator.findElement("Test Text", ElementLocator.LocatorStrategy.TEXT);

        assertSame(mockElement, element);
    }

    @Test
    void testFindElementNotFound() {
        when(mockDriver.findElement(any(By.class)))
            .thenThrow(new NoSuchElementException("not found"));

        assertThrows(NoSuchElementException.class, () -> {
            elementLocator.findElement("#nonexistent", ElementLocator.LocatorStrategy.CSS);
        });
    }

    @Test
    void testSmartFindElement() {
        WebElement mockElement = mock(WebElement.class);

        // CSS selector 成功
        when(mockDriver.findElement(By.cssSelector("#test"))).thenReturn(mockElement);

        WebElement element = elementLocator.smartFindElement("#test");

        assertSame(mockElement, element);
    }

    @Test
    void testSmartFindElementFallback() {
        WebElement mockElement = mock(WebElement.class);

        // CSS 失败，XPath 成功
        when(mockDriver.findElement(By.cssSelector("#test")))
            .thenThrow(new NoSuchElementException("not found"));
        when(mockDriver.findElement(By.xpath("//*[@id='test']")))
            .thenReturn(mockElement);

        WebElement element = elementLocator.smartFindElement("#test");

        assertSame(mockElement, element);
    }

    @Test
    void testFindElements() {
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);

        when(mockDriver.findElements(By.cssSelector(".item")))
            .thenReturn(java.util.List.of(element1, element2));

        var elements = elementLocator.findElements(".item", ElementLocator.LocatorStrategy.CSS);

        assertEquals(2, elements.size());
    }

    @Test
    void testWaitForInteractable() {
        WebElement mockElement = mock(WebElement.class);

        when(mockElement.isDisplayed()).thenReturn(true);
        when(mockElement.isEnabled()).thenReturn(true);
        when(mockDriver.findElement(any(By.class))).thenReturn(mockElement);

        WebElement element = elementLocator.waitForInteractable("#test");

        assertSame(mockElement, element);
    }

    @Test
    void testScrollToElement() {
        WebElement mockElement = mock(WebElement.class);
        when(mockDriver.findElement(By.cssSelector("#test"))).thenReturn(mockElement);

        elementLocator.scrollToElement(mockElement);

        // 验证 JavaScript 执行
        verify(mockDriver, times(1)).executeScript(anyString());
    }

    @Test
    void testIsInViewport() {
        WebElement mockElement = mock(WebElement.class);

        when(mockDriver.executeScript(anyString())).thenReturn(true);

        boolean inViewport = elementLocator.isInViewport(mockElement);

        assertTrue(inViewport);
    }

    @Test
    void testAriaTree() {
        WebElement mockElement = mock(WebElement.class);
        when(mockElement.getTagName()).thenReturn("div");
        when(mockElement.getAttribute("role")).thenReturn("button");
        when(mockElement.getAttribute("aria-label")).thenReturn("Click me");

        String ariaTree = elementLocator.getAriaTree(mockElement);

        assertTrue(ariaTree.contains("div"));
        assertTrue(ariaTree.contains("button"));
        assertTrue(ariaTree.contains("Click me"));
    }

    @Test
    void testLocatorStrategies() {
        // 测试所有定位策略枚举
        ElementLocator.LocatorStrategy[] strategies = ElementLocator.LocatorStrategy.values();

        assertEquals(8, strategies.length);
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.CSS));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.XPATH));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.TEXT));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.ARIA));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.ID));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.NAME));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.CLASS));
        assertTrue(java.util.Arrays.asList(strategies).contains(ElementLocator.LocatorStrategy.TAG));
    }
}
