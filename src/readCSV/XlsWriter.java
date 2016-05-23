package readCSV;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class XlsWriter {
	
	public XlsWriter() {
		
	}
	
//	public void createCells() throws IOException {
//		HSSFWorkbook wb = new HSSFWorkbook();
//        HSSFSheet sheet = wb.createSheet("new sheet");
//
//        for (int i = 0; i < 100; i++) {
//        	// Create a row and put some cells in it. Rows are 0 based.
//            HSSFRow row = sheet.createRow(i);
//            // Create a cell and put a value in it.
//            HSSFCell cell = row.createCell(0);
//            cell.setCellValue(2.9);
//
//            // Or do it on one line.
//            row.createCell(1).setCellValue(1.2);
//            row.createCell(2).setCellValue(1.5);
//            row.createCell(3).setCellValue(2.5);
//        }
//
//        // Write the output to a file
//        FileOutputStream fileOut = new FileOutputStream("/Users/xiayiqian/Downloads/workbook.xls");
//        wb.write(fileOut);
//        fileOut.close();
//
//        wb.close();
//	}
	
	public void exportToExcel(HashMap<String, Integer> testingDataset, Integer[] predictedTimes, String[] urlArr) {
		HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");
        
        int length = urlArr.length;

        HSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("x轴");
        row.createCell(1).setCellValue("predicted");
        row.createCell(2).setCellValue("tested");
        
        for (int i = 0; i < length; i++) {
            HSSFRow dataRow = sheet.createRow(i+1);
            dataRow.createCell(0).setCellValue(urlArr[i]);
            dataRow.createCell(1).setCellValue(predictedTimes[i]);
            
            if (testingDataset.get(urlArr[i]) == null) {
            	dataRow.createCell(2).setCellValue(0);
            }
            else {
            	dataRow.createCell(2).setCellValue(testingDataset.get(urlArr[i]));   
            }
        }

        // Write the output to a file
        FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("/Users/xiayiqian/Downloads/output/finalPrediction.xls");
			wb.write(fileOut);
	        fileOut.close();
	        wb.close();
	        
	        System.out.println("预测数据导出完成！.....");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
