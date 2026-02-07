package com.openclawlite.tools;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Excel 文件生成工具
 */
public class ExcelGenerator {

    public static void main(String[] args) {
        String outputPath = "data/员工信息表.xlsx";
        if (args.length > 0) {
            outputPath = args[0];
        }

        try {
            generateExcel(outputPath);
            System.out.println("✅ Excel文件已成功生成: " + outputPath);
            System.out.println("\n文件内容：");
            System.out.println("┌────────┬────────┬────────┐");
            System.out.println("│ 人员   │ 部门   │ 名称   │");
            System.out.println("├────────┼────────┼────────┤");
            System.out.println("│ 张三   │ 技术部 │ 张三   │");
            System.out.println("│ 李四   │ 市场部 │ 李四   │");
            System.out.println("│ 王五   │ 财务部 │ 王五   │");
            System.out.println("│ 赵六   │ 人事部 │ 赵六   │");
            System.out.println("└────────┴────────┴────────┘");
        } catch (Exception e) {
            System.err.println("❌ 生成Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 生成包含人员、部门、名称的Excel文件
     */
    public static void generateExcel(String outputPath) throws IOException {
        // 确保输出目录存在
        Path path = Paths.get(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建工作表
            Sheet sheet = workbook.createSheet("员工信息");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"人员", "部门", "名称"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据
            Object[][] data = {
                    {"张三", "技术部", "张三"},
                    {"李四", "市场部", "李四"},
                    {"王五", "财务部", "王五"},
                    {"赵六", "人事部", "赵六"}
            };

            // 填充数据
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(String.valueOf(data[i][j]));
                    cell.setCellStyle(dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽，避免列宽太小
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
    private static CellStyle createHeaderStyle(Workbook workbook) {
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
    private static CellStyle createDataStyle(Workbook workbook) {
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
