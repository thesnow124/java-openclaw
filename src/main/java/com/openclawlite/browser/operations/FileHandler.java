package com.openclawlite.browser.operations;

import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文件处理器
 *
 * <p>处理文件上传和下载。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>文件上传（input type=file）</li>
 *   <li>文件下载监控</li>
 *   <li>下载文件管理</li>
 *   <li>文件内容验证</li>
 *   <li>批量文件操作</li>
 * </ul>
 */
public class FileHandler {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);

    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;
    private String downloadDirectory;

    /**
     * 文件下载信息
     */
    public record DownloadInfo(
        String fileName,            // 文件名
        String url,                 // 下载 URL
        long fileSize,              // 文件大小
        String mimeType,            // MIME 类型
        boolean completed,          // 是否完成
        long startTime,             // 开始时间
        long endTime,               // 结束时间
        String filePath             // 文件路径
    ) {
        public long getDuration() {
            return endTime > 0 ? endTime - startTime : 0;
        }
    }

    /**
     * 文件上传信息
     */
    public record UploadInfo(
        String fileName,            // 文件名
        String filePath,            // 文件路径
        long fileSize,              // 文件大小
        boolean success,            // 是否成功
        String errorMessage         // 错误信息
    ) {}

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     */
    public FileHandler(WebDriver driver) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.downloadDirectory = System.getProperty("java.io.tmpdir");
    }

    /**
     * 构造函数
     *
     * @param driver WebDriver 实例
     * @param downloadDirectory 下载目录
     */
    public FileHandler(WebDriver driver, String downloadDirectory) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.downloadDirectory = downloadDirectory;
    }

    // ==================== 文件上传 ====================

    /**
     * 上传单个文件
     *
     * @param fileInputSelector 文件输入元素选择器
     * @param filePath 文件路径
     * @return UploadInfo 上传信息
     */
    public UploadInfo uploadFile(String fileInputSelector, String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return new UploadInfo(file.getName(), filePath, 0, false, "文件不存在");
        }

        try {
            WebElement fileInput = driver.findElement(By.cssSelector(fileInputSelector));

            // 使用 JavaScript 设置文件（更可靠）
            jsExecutor.executeScript(
                "arguments[0].style.display = 'block';",
                fileInput
            );

            fileInput.sendKeys(filePath);

            log.info("上传文件: selector={}, file={}", fileInputSelector, filePath);

            return new UploadInfo(file.getName(), filePath, file.length(), true, null);

        } catch (Exception e) {
            log.error("上传文件失败: file={}", filePath, e);
            return new UploadInfo(file.getName(), filePath, file.length(), false, e.getMessage());
        }
    }

    /**
     * 上传多个文件
     *
     * @param fileInputSelector 文件输入元素选择器
     * @param filePaths 文件路径数组
     * @return List<UploadInfo> 上传信息列表
     */
    public List<UploadInfo> uploadMultipleFiles(String fileInputSelector, String... filePaths) {
        List<UploadInfo> results = new ArrayList<>();

        try {
            WebElement fileInput = driver.findElement(By.cssSelector(fileInputSelector));

            // 检查是否支持多文件上传
            String multiple = fileInput.getAttribute("multiple");
            if (multiple == null) {
                log.warn("文件输入不支持多文件上传");
            }

            // 构建文件路径字符串
            StringBuilder filePathBuilder = new StringBuilder();
            for (int i = 0; i < filePaths.length; i++) {
                if (i > 0) {
                    filePathBuilder.append("\n");
                }
                filePathBuilder.append(filePaths[i]);
            }

            fileInput.sendKeys(filePathBuilder.toString());

            log.info("上传多个文件: selector={}, count={}", fileInputSelector, filePaths.length);

            // 创建上传信息
            for (String filePath : filePaths) {
                File file = new File(filePath);
                results.add(new UploadInfo(file.getName(), filePath, file.length(), true, null));
            }

        } catch (Exception e) {
            log.error("上传多个文件失败", e);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                results.add(new UploadInfo(file.getName(), filePath, file.length(), false, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 使用 Base64 上传文件（适用于某些特殊场景）
     *
     * @param fileInputSelector 文件输入元素选择器
     * @param filePath 文件路径
     * @return UploadInfo 上传信息
     */
    public UploadInfo uploadFileAsBase64(String fileInputSelector, String filePath) {
        try {
            File file = new File(filePath);

            // 读取文件并转换为 Base64
            byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileContent);

            // 创建 Data URL
            String dataUrl = "data:application/octet-stream;base64," + base64Content;

            // 使用 JavaScript 设置文件内容
            WebElement fileInput = driver.findElement(By.cssSelector(fileInputSelector));
            jsExecutor.executeScript(
                "var input = arguments[0];" +
                "var dataUrl = arguments[1];" +
                "" +
                "// 创建 File 对象" +
                "var byteCharacters = atob(dataUrl.split(',')[1]);" +
                "var byteNumbers = new Array(byteCharacters.length);" +
                "for (var i = 0; i < byteCharacters.length; i++) {" +
                "  byteNumbers[i] = byteCharacters.charCodeAt(i);" +
                "}" +
                "var byteArray = new Uint8Array(byteNumbers);" +
                "var file = new File([byteArray], arguments[2]);" +
                "" +
                "// 创建 DataTransfer 对象并设置文件" +
                "var dataTransfer = new DataTransfer();" +
                "dataTransfer.items.add(file);" +
                "input.files = dataTransfer.files;",
                fileInput, dataUrl, file.getName()
            );

            log.info("使用 Base64 上传文件: file={}", filePath);

            return new UploadInfo(file.getName(), filePath, file.length(), true, null);

        } catch (Exception e) {
            log.error("使用 Base64 上传文件失败: file={}", filePath, e);
            File file = new File(filePath);
            return new UploadInfo(file.getName(), filePath, file.length(), false, e.getMessage());
        }
    }

    // ==================== 文件下载 ====================

    /**
     * 设置下载目录
     *
     * @param directory 下载目录
     */
    public void setDownloadDirectory(String directory) {
        this.downloadDirectory = directory;
        log.info("设置下载目录: {}", directory);
    }

    /**
     * 获取下载目录
     *
     * @return String 下载目录
     */
    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    /**
     * 点击下载链接
     *
     * @param downloadLinkSelector 下载链接选择器
     * @return DownloadInfo 下载信息
     */
    public DownloadInfo clickDownload(String downloadLinkSelector) {
        try {
            WebElement downloadLink = driver.findElement(By.cssSelector(downloadLinkSelector));

            // 获取下载 URL
            String downloadUrl = downloadLink.getAttribute("href");
            String fileName = extractFileNameFromUrl(downloadUrl);

            // 获取下载前的文件列表
            Set<String> filesBefore = listDownloadDirectory();

            // 点击下载链接
            long startTime = System.currentTimeMillis();
            downloadLink.click();

            // 等待文件下载完成
            String downloadedFile = waitForDownloadComplete(filesBefore, 60);

            long endTime = System.currentTimeMillis();

            if (downloadedFile != null) {
                File file = new File(downloadDirectory, downloadedFile);

                log.info("文件下载成功: file={}, size={}", downloadedFile, file.length());

                return new DownloadInfo(
                    downloadedFile,
                    downloadUrl,
                    file.length(),
                    Files.probeContentType(file.toPath()),
                    true,
                    startTime,
                    endTime,
                    file.getAbsolutePath()
                );
            } else {
                log.warn("文件下载超时或失败: url={}", downloadUrl);
                return new DownloadInfo(fileName, downloadUrl, 0, null, false, startTime, endTime, null);
            }

        } catch (Exception e) {
            log.error("点击下载链接失败: selector={}", downloadLinkSelector, e);
            return new DownloadInfo(null, null, 0, null, false, 0, 0, null);
        }
    }

    /**
     * 直接下载文件（使用 URL）
     *
     * @param downloadUrl 下载 URL
     * @return DownloadInfo 下载信息
     */
    public DownloadInfo downloadFile(String downloadUrl) {
        try {
            String fileName = extractFileNameFromUrl(downloadUrl);

            // 获取下载前的文件列表
            Set<String> filesBefore = listDownloadDirectory();

            // 使用 JavaScript 触发下载
            jsExecutor.executeScript(
                "var link = document.createElement('a');" +
                "link.href = arguments[0];" +
                "link.download = arguments[1];" +
                "document.body.appendChild(link);" +
                "link.click();" +
                "document.body.removeChild(link);",
                downloadUrl, fileName
            );

            // 等待文件下载完成
            long startTime = System.currentTimeMillis();
            String downloadedFile = waitForDownloadComplete(filesBefore, 60);
            long endTime = System.currentTimeMillis();

            if (downloadedFile != null) {
                File file = new File(downloadDirectory, downloadedFile);

                log.info("文件下载成功: file={}, size={}", downloadedFile, file.length());

                return new DownloadInfo(
                    downloadedFile,
                    downloadUrl,
                    file.length(),
                    Files.probeContentType(file.toPath()),
                    true,
                    startTime,
                    endTime,
                    file.getAbsolutePath()
                );
            } else {
                log.warn("文件下载超时或失败: url={}", downloadUrl);
                return new DownloadInfo(fileName, downloadUrl, 0, null, false, startTime, endTime, null);
            }

        } catch (Exception e) {
            log.error("下载文件失败: url={}", downloadUrl, e);
            return new DownloadInfo(null, downloadUrl, 0, null, false, 0, 0, null);
        }
    }

    /**
     * 等待下载完成
     *
     * @param filesBefore 下载前的文件集合
     * @param timeoutSeconds 超时时间（秒）
     * @return String 下载的文件名，失败返回 null
     */
    private String waitForDownloadComplete(Set<String> filesBefore, int timeoutSeconds) {
        int maxAttempts = timeoutSeconds * 10; // 每 100ms 检查一次

        for (int i = 0; i < maxAttempts; i++) {
            try {
                Thread.sleep(100);

                Set<String> filesAfter = listDownloadDirectory();

                // 找出新文件
                filesAfter.removeAll(filesBefore);

                if (!filesAfter.isEmpty()) {
                    // 检查是否有 .crdownload 或 .tmp 临时文件（表示正在下载）
                    boolean hasTempFile = false;

                    for (String fileName : filesAfter) {
                        if (fileName.endsWith(".crdownload") ||
                            fileName.endsWith(".tmp") ||
                            fileName.endsWith(".part")) {
                            hasTempFile = true;
                            break;
                        }
                    }

                    // 如果没有临时文件，说明下载完成
                    if (!hasTempFile) {
                        return filesAfter.iterator().next();
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null;
    }

    /**
     * 列出下载目录中的文件
     *
     * @return Set<String> 文件名集合
     */
    private Set<String> listDownloadDirectory() {
        Set<String> fileNames = new HashSet<>();

        File downloadDir = new File(downloadDirectory);
        if (downloadDir.exists() && downloadDir.isDirectory()) {
            File[] files = downloadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    fileNames.add(file.getName());
                }
            }
        }

        return fileNames;
    }

    /**
     * 从 URL 提取文件名
     *
     * @param url URL
     * @return String 文件名
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "download";
        }

        try {
            String path = url.split("\\?")[0]; // 移除查询参数
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            if (fileName.isEmpty()) {
                return "download";
            }

            return fileName;

        } catch (Exception e) {
            return "download";
        }
    }

    // ==================== 文件管理 ====================

    /**
     * 获取下载目录中的所有文件
     *
     * @return List<File> 文件列表
     */
    public List<File> getDownloadedFiles() {
        List<File> files = new ArrayList<>();

        File downloadDir = new File(downloadDirectory);
        if (downloadDir.exists() && downloadDir.isDirectory()) {
            File[] fileArray = downloadDir.listFiles();
            if (fileArray != null) {
                files.addAll(Arrays.asList(fileArray));
            }
        }

        return files;
    }

    /**
     * 清空下载目录
     *
     * @return int 删除的文件数量
     */
    public int clearDownloadDirectory() {
        int deletedCount = 0;

        List<File> files = getDownloadedFiles();

        for (File file : files) {
            if (file.delete()) {
                deletedCount++;
                log.debug("删除文件: {}", file.getName());
            } else {
                log.warn("删除文件失败: {}", file.getName());
            }
        }

        log.info("清空下载目录: deleted={}", deletedCount);
        return deletedCount;
    }

    /**
     * 验证文件是否存在
     *
     * @param fileName 文件名
     * @return boolean 是否存在
     */
    public boolean isFileExists(String fileName) {
        File file = new File(downloadDirectory, fileName);
        return file.exists();
    }

    /**
     * 获取文件大小
     *
     * @param fileName 文件名
     * @return long 文件大小（字节）
     */
    public long getFileSize(String fileName) {
        File file = new File(downloadDirectory, fileName);
        return file.exists() ? file.length() : 0;
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return boolean 是否成功
     */
    public boolean deleteFile(String fileName) {
        File file = new File(downloadDirectory, fileName);
        boolean success = file.delete();

        if (success) {
            log.info("删除文件: {}", fileName);
        } else {
            log.warn("删除文件失败: {}", fileName);
        }

        return success;
    }

    /**
     * 验证文件内容（MD5）
     *
     * @param fileName 文件名
     * @param expectedMd5 期望的 MD5 值
     * @return boolean 是否匹配
     */
    public boolean verifyFileMd5(String fileName, String expectedMd5) {
        try {
            File file = new File(downloadDirectory, fileName);

            if (!file.exists()) {
                return false;
            }

            // 计算 MD5
            String actualMd5 = calculateMd5(file);

            boolean matches = expectedMd5.equalsIgnoreCase(actualMd5);

            log.info("验证文件 MD5: file={}, expected={}, actual={}, matches={}",
                     fileName, expectedMd5, actualMd5, matches);

            return matches;

        } catch (Exception e) {
            log.error("验证文件 MD5 失败: file={}", fileName, e);
            return false;
        }
    }

    /**
     * 计算 MD5
     *
     * @param file 文件
     * @return String MD5 值
     * @throws Exception 异常
     */
    private String calculateMd5(File file) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
        byte[] hash = md.digest(fileContent);

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
