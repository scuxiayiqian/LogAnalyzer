package readCSV;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import java.io.FileOutputStream;
import java.util.HashMap;

public class XlsWriter {
	
	private String outputPath = "";
	
	public XlsWriter(String path) {
		this.outputPath = path;
	}
	
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
			fileOut = new FileOutputStream(outputPath);
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
