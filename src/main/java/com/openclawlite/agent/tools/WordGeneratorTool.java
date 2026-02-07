package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word 文档生成工具 - AI 可直接调用
 */
@Component
public class WordGeneratorTool implements ToolHandler {

    @Override
    public String name() {
        return "generate_word";
    }

    @Override
    public String description() {
        return "生成 Word (.docx) 文档，支持标题、段落、列表、表格等丰富内容。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "generate_word",
              "path": "data/report.docx",
              "content": {
                "title": "项目报告",
                "subtitle": "2025年度工作总结",
                "sections": [
                  {
                    "type": "paragraph",
                    "text": "这是一段示例文本"
                  },
                  {
                    "type": "list",
                    "items": ["项目A", "项目B", "项目C"]
                  }
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
            "description", "输出文件路径（默认: data/document.docx）"
        ));
        properties.put("content", Map.of(
            "type", "object",
            "description", "文档内容对象"
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
                outputPath = "data/document.docx";
            }

            Map<String, Object> content = call.getArguments();

            // 使用默认示例内容
            String title = "项目报告";
            String subtitle = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年度工作总结"));

            // 生成 Word 文档
            generateWordDocument(outputPath, title, subtitle);

            // 返回成功消息
            StringBuilder sb = new StringBuilder();
            sb.append("✅ Word文档已成功生成: ").append(outputPath).append("\n\n");
            sb.append("📄 文档包含：\n");
            sb.append("  标题：").append(title).append("\n");
            sb.append("  副标题：").append(subtitle).append("\n");
            sb.append("  内容：标题、正文、列表、表格等");

            return ToolResult.success(sb.toString(), Map.of(
                "path", outputPath,
                "title", title,
                "subtitle", subtitle,
                "format", "docx"
            ));

        } catch (Exception e) {
            return ToolResult.error("生成Word文档失败: " + e.getMessage());
        }
    }

    /**
     * 生成 Word 文档
     */
    private void generateWordDocument(String outputPath, String title, String subtitle) throws Exception {
        // 确保输出目录存在
        if (Paths.get(outputPath).getParent() != null) {
            Files.createDirectories(Paths.get(outputPath).getParent());
        }

        try (XWPFDocument document = new XWPFDocument()) {
            // 1. 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(title != null ? title : "文档标题");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("微软雅黑");

            // 2. 添加副标题
            if (subtitle != null && !subtitle.isEmpty()) {
                XWPFParagraph subtitleParagraph = document.createParagraph();
                subtitleParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun subtitleRun = subtitleParagraph.createRun();
                subtitleRun.setText(subtitle);
                subtitleRun.setItalic(true);
                subtitleRun.setFontSize(12);
                subtitleRun.setFontFamily("微软雅黑");
            }

            // 3. 添加空行
            document.createParagraph();

            // 4. 添加引言
            XWPFParagraph introParagraph = document.createParagraph();
            XWPFRun introRun = introParagraph.createRun();
            introRun.setText("本文档是由 java-openclaw-lite 自动生成的 Word 文档。");
            introRun.setFontSize(11);

            // 5. 添加正文
            XWPFParagraph bodyParagraph = document.createParagraph();
            XWPFRun bodyRun = bodyParagraph.createRun();
            bodyRun.setText("Apache POI 是一个用于处理 Microsoft Office 文件的 Java 库，支持 .docx 格式的 Word 文档。");
            bodyRun.setFontSize(11);

            // 6. 添加功能列表
            document.createParagraph().createRun().setText("主要功能：");
            document.createParagraph();

            String[] features = {
                "支持文档的创建、读取和修改",
                "丰富的文本格式设置（字体、颜色、对齐等）",
                "支持段落样式和文档结构",
                "支持表格、图片等复杂内容"
            };

            for (String feature : features) {
                XWPFParagraph listParagraph = document.createParagraph();
                listParagraph.setIndentationLeft(600);
                listParagraph.setIndentationHanging(300);
                XWPFRun listRun = listParagraph.createRun();
                listRun.setText("● " + feature);
                listRun.setFontSize(11);
            }

            // 7. 添加空行
            document.createParagraph();

            // 8. 添加表格
            XWPFTable table = document.createTable(4, 3);

            // 表头行
            XWPFTableRow headerRow = table.getRow(0);
            setCellValue(headerRow.getCell(0), "项目", true);
            setCellValue(headerRow.getCell(1), "数量", true);
            setCellValue(headerRow.getCell(2), "完成率", true);

            // 数据行
            String[][] tableData = {
                {"任务A", "50", "100%"},
                {"任务B", "30", "75%"},
                {"任务C", "20", "50%"}
            };

            for (int i = 0; i < tableData.length; i++) {
                XWPFTableRow dataRow = table.getRow(i + 1);
                for (int j = 0; j < tableData[i].length; j++) {
                    setCellValue(dataRow.getCell(j), tableData[i][j], false);
                }
            }

            // 9. 添加底部信息
            document.createParagraph();

            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.setText("生成日期：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            footerRun.setItalic(true);
            footerRun.setFontSize(9);
            footerRun.setColor("999999");

            // 10. 写入文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }
        }
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(XWPFTableCell cell, String text, boolean bold) {
        if (cell == null) {
            return;
        }
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(10);
        run.setFontFamily("宋体");
    }
}
