import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * 生成 Word (.docx) 文件
 * 使用 Apache POI XWPF 库
 */
public class GenerateDocx {
    
    public static void main(String[] args) {
        String outputPath = "data/document.docx";
        
        try (XWPFDocument document = new XWPFDocument()) {
            // ==================== 标题 ====================
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            title.setSpacingAfter(300); // 段后间距
            
            XWPFRun titleRun = title.createRun();
            titleRun.setText("项目报告");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("宋体");
            
            // ==================== 副标题 ====================
            XWPFParagraph subtitle = document.createParagraph();
            subtitle.setAlignment(ParagraphAlignment.CENTER);
            subtitle.setSpacingAfter(400);
            
            XWPFRun subtitleRun = subtitle.createRun();
            subtitleRun.setText("2025年度工作总结");
            subtitleRun.setItalic(true);
            subtitleRun.setFontSize(12);
            subtitleRun.setFontFamily("宋体");
            
            // ==================== 正文段落 ====================
            XWPFParagraph para1 = document.createParagraph();
            para1.setAlignment(ParagraphAlignment.LEFT);
            para1.setSpacingAfter(200);
            para1.setIndentationFirstLine(500); // 首行缩进
            
            XWPFRun run1 = para1.createRun();
            run1.setText("本文档是一个自动生成的 Word 文档示例。使用了 Apache POI XWPF 库来创建和格式化文档内容。");
            run1.setFontSize(12);
            run1.setFontFamily("宋体");
            
            XWPFParagraph para2 = document.createParagraph();
            para2.setAlignment(ParagraphAlignment.LEFT);
            para2.setSpacingAfter(200);
            para2.setIndentationFirstLine(500);
            
            XWPFRun run2 = para2.createRun();
            run2.setText("Apache POI 是一个用于处理 Microsoft Office 文件的 Java 库，支持 Excel、Word、PowerPoint 等多种格式。");
            run2.setFontSize(12);
            run2.setFontFamily("宋体");
            
            // ==================== 分割线 ====================
            XWPFParagraph line = document.createParagraph();
            line.setSpacingAfter(300);
            XWPFRun lineRun = line.createRun();
            lineRun.setText("——————————————————————————————");
            lineRun.setFontSize(10);
            
            // ==================== 列表 ====================
            XWPFParagraph listTitle = document.createParagraph();
            listTitle.setSpacingBefore(200);
            listTitle.setSpacingAfter(150);
            
            XWPFRun listTitleRun = listTitle.createRun();
            listTitleRun.setText("主要功能：");
            listTitleRun.setBold(true);
            listTitleRun.setFontSize(14);
            
            // 创建列表项
            String[] items = {
                "支持文档的创建、读取和修改",
                "丰富的文本格式设置（字体、颜色、对齐等）",
                "支持段落样式和文档结构",
                "支持表格、图片等复杂内容"
            };
            
            for (String item : items) {
                XWPFParagraph bullet = document.createParagraph();
                bullet.setIndentationLeft(500); // 左缩进
                bullet.setSpacingAfter(100);
                
                XWPFRun bulletRun = bullet.createRun();
                bulletRun.setText("● " + item);
                bulletRun.setFontSize(12);
                bulletRun.setFontFamily("宋体");
            }
            
            // ==================== 表格 ====================
            XWPFParagraph tableTitle = document.createParagraph();
            tableTitle.setSpacingBefore(300);
            tableTitle.setSpacingAfter(150);
            
            XWPFRun tableTitleRun = tableTitle.createRun();
            tableTitleRun.setText("数据统计：");
            tableTitleRun.setBold(true);
            tableTitleRun.setFontSize(14);
            
            // 创建表格：3列4行
            XWPFTable table = document.createTable();
            table.setWidth("90%");
            
            // 设置表格样式
            CTTblWidth tblWidth = table.getCTTbl().getTblPr().getTblW();
            tblWidth.setW(BigInteger.valueOf(9000));
            tblWidth.setType(STTblWidth.DXA);
            
            // 表头行
            XWPFTableRow headerRow = table.getRow(0);
            setCellText(headerRow.getCell(0), "项目", true, "宋体", 12);
            setCellText(headerRow.addNewTableCell(), "数量", true, "宋体", 12);
            setCellText(headerRow.addNewTableCell(), "完成率", true, "宋体", 12);
            
            // 数据行
            String[][] tableData = {
                {"任务A", "50", "100%"},
                {"任务B", "30", "75%"},
                {"任务C", "20", "50%"}
            };
            
            for (String[] rowData : tableData) {
                XWPFTableRow dataRow = table.createRow();
                setCellText(dataRow.getCell(0), rowData[0], false, "宋体", 12);
                setCellText(dataRow.getCell(1), rowData[1], false, "宋体", 12);
                setCellText(dataRow.getCell(2), rowData[2], false, "宋体", 12);
            }
            
            // ==================== 底部信息 ====================
            XWPFParagraph footer = document.createParagraph();
            footer.setAlignment(ParagraphAlignment.RIGHT);
            footer.setSpacingBefore(500);
            
            XWPFRun footerRun = footer.createRun();
            footerRun.setText("生成日期：2025-01-04");
            footerRun.setFontSize(10);
            footerRun.setItalic(true);
            footerRun.setColor("888888");
            
            // ==================== 保存文件 ====================
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
                System.out.println("✅ Word文件已成功生成: " + outputPath);
            }
            
        } catch (IOException e) {
            System.err.println("❌ 生成文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 设置单元格文本和样式
     */
    private static void setCellText(XWPFTableCell cell, String text, boolean bold, String fontFamily, int fontSize) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        
        // 居中对齐
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        
        // 设置垂直居中
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }
}
