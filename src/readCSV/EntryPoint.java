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
			csvReader = new CsvReader("/Users/xiayiqian/Downloads/data.csv", "/Users/xiayiqian/Downloads/output41/");  // 设置1 - 文件输入输出地址
		}
		else {
			csvReader = new CsvReader(args[0], args[1]);
		}
		
		csvReader.setTimes(15952);  // 设置2 - 统计的数据有多少条
		
		csvReader.read();
		csvReader.handleDatas();
		csvReader.printHashTable();
		csvReader.writeMatrixToFile();
		csvReader.printTransitionMatrix(csvReader.transionMatrix);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date timepoint;
		try {
			timepoint = df.parse("2016-03-08 11:31:21");  // 设置3 - 从哪个时间点开始分析次数
			double[] weightarr = {0, 0, 0, 1};     // 设置4 - 权重数组
			csvReader.collectRSTimes(timepoint, 300000, 4, weightarr);   // 设置5 - 分析多久时间内的次数
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("in parse exception");
		}
		
	}
	
}
