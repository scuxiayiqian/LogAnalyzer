package readCSV;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntryPoint {

	public static void main(String args[]) throws IOException
	{	
		
		CsvReader csvReader;		
		
		if (args.length == 0) {
			csvReader = new CsvReader("/Users/xiayiqian/Downloads/data.csv", "/Users/xiayiqian/Downloads/output/");
		}
		else {
			csvReader = new CsvReader(args[0], args[1]);
		}
		
		csvReader.setTimes(100000);  // 设置1 - 统计的数据有多少条
		
		csvReader.read();
		csvReader.handleDatas();
		csvReader.printHashTable();
		csvReader.writeMatrixToFile();
		csvReader.printTransitionMatrix(csvReader.transionMatrix);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date timepoint;
		try {
			timepoint = df.parse("2016-03-11 07:07:56");  // 设置2 - 从哪个时间点开始分析次数
			csvReader.collectRSTimes(timepoint, 300000);   // 设置3 - 分析多久时间内的次数
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("in parse exception");
		}
		
	}
	
}
