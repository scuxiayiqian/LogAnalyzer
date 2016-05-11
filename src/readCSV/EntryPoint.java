package readCSV;

public class EntryPoint {

	public static void main(String args[])
	{	
		CsvReader csvReader;
		if (args.length == 0) {
			csvReader = new CsvReader("/Users/xiayiqian/Downloads/data.csv", "/Users/xiayiqian/Downloads/output/");
		}
		else {
			csvReader = new CsvReader(args[0], args[1]);
		}
		csvReader.read();
		csvReader.handleDatas();
		csvReader.printHashTable();
		csvReader.writeMatrixToFile();
	}
	
}
