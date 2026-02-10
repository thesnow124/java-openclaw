package com.openclawlite.browser.operations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StorageManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
class StorageManagerTest {

    @Mock
    private WebDriver mockDriver;

    private StorageManager storageManager;

    @BeforeEach
    void setUp() {
        when(mockDriver.manage()).thenReturn(new org.openqa.selenium.WebDriver.Options() {
            @Override
            public void deleteCookieNamed(String name) {}

            @Override
            public org.openqa.selenium.Cookie getCookieNamed(String name) {
                return null;
            }

            @Override
            public java.util.Set<org.openqa.selenium.Cookie> getCookies() {
                return java.util.Set.of();
            }

            @Override
            public void addCookie(org.openqa.selenium.Cookie cookie) {}

            @Override
            public void deleteAllCookies() {}

            @Override
            public org.openqa.selenium.WebDriver.TargetLocator window() {
                return null;
            }

            @Override
            public org.openqa.selenium.WebDriver.Window window() {
                return null;
            }

            @Override
            public org.openqa.selenium.WebDriver.Navigation navigate() {
                return null;
            }

            @Override
            public org.openqa.selenium.WebDriver.ImeHandler ime() {
                return null;
            }

            @Override
            public org.openqa.selenium.WebDriver.Options timeouts() {
                return null;
            }

            @Override
            public org.openqa.selenium.WebDriver logs() {
                return null;
            }
        });

        storageManager = new StorageManager(mockDriver);
    }

    @Test
    void testGetAllCookies() {
        java.util.List<StorageManager.CookieInfo> cookies = storageManager.getAllCookies();

        assertNotNull(cookies);
        assertTrue(cookies.isEmpty());
    }

    @Test
    void testAddCookie() {
        boolean success = storageManager.addCookie("test", "value");

        assertTrue(success);
    }

    @Test
    void testDeleteCookie() {
        boolean success = storageManager.deleteCookie("test");

        assertTrue(success);
    }

    @Test
    void testDeleteAllCookies() {
        int count = storageManager.deleteAllCookies();

        assertEquals(0, count);
    }

    @Test
    void testGetLocalStorageItem() {
        when(mockDriver.executeScript(anyString())).thenReturn(null);

        String value = storageManager.getLocalStorageItem("testKey");

        assertNull(value);
    }

    @Test
    void testSetLocalStorageItem() {
        boolean success = storageManager.setLocalStorageItem("testKey", "testValue");

        assertTrue(success);
    }

    @Test
    void testRemoveLocalStorageItem() {
        boolean success = storageManager.removeLocalStorageItem("testKey");

        assertTrue(success);
    }

    @Test
    void testClearLocalStorage() {
        when(mockDriver.executeScript(anyString())).thenReturn(2L);

        int count = storageManager.clearLocalStorage();

        assertEquals(2, count);
    }

    @Test
    void testGetLocalStorageLength() {
        when(mockDriver.executeScript(anyString())).thenReturn(5L);

        int length = storageManager.getLocalStorageLength();

        assertEquals(5, length);
    }

    @Test
    void testGetSessionStorageItem() {
        when(mockDriver.executeScript(anyString())).thenReturn(null);

        String value = storageManager.getSessionStorageItem("testKey");

        assertNull(value);
    }

    @Test
    void testSetSessionStorageItem() {
        boolean success = storageManager.setSessionStorageItem("testKey", "testValue");

        assertTrue(success);
    }

    @Test
    void testRemoveSessionStorageItem() {
        boolean success = storageManager.removeSessionStorageItem("testKey");

        assertTrue(success);
    }

    @Test
    void testClearSessionStorage() {
        when(mockDriver.executeScript(anyString())).thenReturn(3L);

        int count = storageManager.clearSessionStorage();

        assertEquals(3, count);
    }

    @Test
    void testGetSessionStorageLength() {
        when(mockDriver.executeScript(anyString())).thenReturn(4L);

        int length = storageManager.getSessionStorageLength();

        assertEquals(4, length);
    }

    @Test
    void testClearAllStorage() {
        when(mockDriver.executeScript(anyString())).thenReturn(2L).thenReturn(3L);

        java.util.Map<String, Integer> result = storageManager.clearAllStorage();

        assertNotNull(result);
        assertTrue(result.containsKey("cookies"));
        assertTrue(result.containsKey("localStorage"));
        assertTrue(result.containsKey("sessionStorage"));
    }

    @Test
    void testGetStorageStats() {
        when(mockDriver.executeScript(anyString())).thenReturn(2L).thenReturn(3L);

        java.util.Map<String, Object> stats = storageManager.getStorageStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("cookies"));
        assertTrue(stats.containsKey("localStorage"));
        assertTrue(stats.containsKey("sessionStorage"));
        assertTrue(stats.containsKey("url"));
        assertTrue(stats.containsKey("timestamp"));
    }

    @Test
    void testCookieInfoRecord() {
        StorageManager.CookieInfo cookie = new StorageManager.CookieInfo(
            "name", "value", "domain", "/path",
            new java.util.Date(), true, true
        );

        assertEquals("name", cookie.name());
        assertEquals("value", cookie.value());
        assertEquals("domain", cookie.domain());
        assertEquals("/path", cookie.path());
        assertTrue(cookie.secure());
        assertTrue(cookie.httpOnly());
    }

    @Test
    void testStorageItemRecord() {
        StorageManager.StorageItem item = new StorageManager.StorageItem(
            "key", "value"
        );

        assertEquals("key", item.key());
        assertEquals("value", item.value());
    }

    @Test
    void testImportCookieFromJson() {
        String jsonCookie = "{\"name\":\"test\",\"value\":\"value\"}";

        boolean success = storageManager.importCookieFromJson(jsonCookie);

        assertTrue(success);
    }

    @Test
    void testExportCookiesAsJson() {
        String json = storageManager.exportCookiesAsJson();

        assertNotNull(json);
        assertTrue(json.contains("[")); // JSON array
    }
}
