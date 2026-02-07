import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成Excel文件的Java脚本
 * 功能：创建包含人员、部门、名称列的.xlsx文件
 * 依赖：Apache POI 5.2.3+
 */
public class GenerateExcel {
    
    public static void main(String[] args) {
        // Excel文件路径
        String excelFilePath = "data/excel.xlsx";
        
        // 准备数据
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("张三", "技术部", "张三"));
        employees.add(new Employee("李四", "市场部", "李四"));
        employees.add(new Employee("王五", "财务部", "王五"));
        employees.add(new Employee("赵六", "人事部", "赵六"));
        
        try {
            generateExcel(excelFilePath, employees);
            System.out.println("✅ Excel文件已成功生成: " + excelFilePath);
        } catch (IOException e) {
            System.err.println("❌ 生成Excel文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void generateExcel(String filePath, List<Employee> employees) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("员工信息");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"人员", "部门", "名称"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(emp.getPerson());
                row.createCell(1).setCellValue(emp.getDepartment());
                row.createCell(2).setCellValue(emp.getName());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    static class Employee {
        private String person;
        private String department;
        private String name;
        
        public Employee(String person, String department, String name) {
            this.person = person;
            this.department = department;
            this.name = name;
        }
        
        public String getPerson() { return person; }
        public String getDepartment() { return department; }
        public String getName() { return name; }
    }
}