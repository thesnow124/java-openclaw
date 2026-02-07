package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件生成工具 - AI 可直接调用
 */
@Component
public class ExcelGeneratorTool implements ToolHandler {

    @Override
    public String name() {
        return "generate_excel";
    }

    @Override
    public String description() {
        return "生成包含指定列和数据的 Excel (.xlsx) 文件。支持自定义表头、数据和样式。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "generate_excel",
              "path": "data/output.xlsx",
              "content": {
                "headers": ["人员", "部门", "名称"],
                "data": [
                  ["张三", "技术部", "张三"],
                  ["李四", "市场部", "李四"]
                ]
              }
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("path", Map.of(
            "type", "string",
            "description", "输出文件路径（默认: data/output.xlsx）"
        ));
        properties.put("headers", Map.of(
            "type", "array",
            "items", Map.of("type", "string"),
            "description", "表头列名数组"
        ));
        properties.put("data", Map.of(
            "type", "array",
            "items", Map.of("type", "array"),
            "description", "数据行数组，每行是一个数组"
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of()
        );
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        ToolResult result = executeTyped(call, context);
        return result.getText();
    }

    @Override
    public ToolResult executeTyped(ToolCall call, ToolContext context) {
        try {
            // 获取参数
            String outputPath = call.getPath();
            if (outputPath == null || outputPath.trim().isEmpty()) {
                outputPath = "data/output.xlsx";
            }

            // 从 content 中解析数据
            Map<String, Object> content = call.getArguments();
            List<String> headers = (List<String>) content.get("headers");
            List<List<String>> data = (List<List<String>>) content.get("data");

            // 如果没有提供数据，使用默认示例
            if (headers == null || headers.isEmpty()) {
                headers = List.of("人员", "部门", "名称");
            }
            if (data == null || data.isEmpty()) {
                data = List.of(
                    List.of("张三", "技术部", "张三"),
                    List.of("李四", "市场部", "李四"),
                    List.of("王五", "财务部", "王五"),
                    List.of("赵六", "人事部", "赵六")
                );
            }

            // 生成 Excel 文件
            generateExcel(outputPath, headers, data);

            // 返回成功消息
            StringBuilder sb = new StringBuilder();
            sb.append("✅ Excel文件已成功生成: ").append(outputPath).append("\n\n");
            sb.append("文件包含 ").append(headers.size()).append(" 列，").append(data.size()).append(" 行数据。\n");
            sb.append("表头: ").append(String.join(", ", headers)).append("\n");

            return ToolResult.success(sb.toString(), Map.of(
                "path", outputPath,
                "rows", data.size(),
                "columns", headers.size(),
                "headers", headers
            ));

        } catch (Exception e) {
            return ToolResult.error("生成Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 生成 Excel 文件
     */
    private void generateExcel(String outputPath, List<String> headers, List<List<String>> data) throws Exception {
        // 确保输出目录存在
        Path path = Paths.get(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet("Sheet1");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            for (int i = 0; i < data.size(); i++) {
                List<String> row = data.get(i);
                Row dataRow = sheet.createRow(i + 1);
                for (int j = 0; j < Math.min(row.size(), headers.size()); j++) {
                    Cell cell = dataRow.createCell(j);
                    cell.setCellValue(row.get(j));
                    cell.setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // 对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // 对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }
}
