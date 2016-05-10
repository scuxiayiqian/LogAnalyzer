package readCSV;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

public class CsvReader {
	
	private String inputPath;
	private String outputPath;
	private int interval = 300;
	private int times = 1000;
	private int groupNumber = -1;
	private ArrayList<Integer> groupNumberArr = new ArrayList<Integer>();
	private HashMap<Integer, ArrayList<LogRecord>> logRecordTable = new HashMap<Integer, ArrayList<LogRecord>>();
	private HashMap<String, ArrayList<ResourceInfo>> dataTable = new HashMap<String, ArrayList<ResourceInfo>>();
	
	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
    public CsvReader(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }
    
    public void read() {
    	try {
            String encoding="GBK";
            File file=new File(inputPath);
            
            if(file.isFile() && file.exists()) { //判断文件是否存在	
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                
                int i = 0;
                while(((lineTxt = bufferedReader.readLine()) != null) && (i < times)){
                	
                	if (i == 0) {
                		i++;
                		continue;
                	}
                	
                	groupNumber = getTimeGroup(lineTxt, i);  
                	if (!groupNumberArr.contains(groupNumber)) {
                		groupNumberArr.add(groupNumber);
                	}
                	parserWrite(groupNumber, lineTxt); // append行到对应group的csv文件中
                	appendToLogRecordTable(groupNumber, lineTxt);  // 将log以hash<array>的方式存入内存，以便之后分组处理的时候不用再去读文件
                	i++;
                	
                }
                read.close();
		    }
            else {
		        System.out.println("找不到指定的文件");
		    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }    
    }
    
    public void handleDatas() {
    	for (int groupNum:groupNumberArr) {
    		ArrayList<String> userIdArr = new ArrayList<String>();
    		userIdArr = getAllUserId(groupNum);
    		handleDataByGroup(groupNum, userIdArr);
    	}
    }
    
    private ArrayList<String> getAllUserId(int groupNumber) {
    	
    	ArrayList<String> userIdArr = new ArrayList<String>();
    	
    	try {
            String encoding="GBK";
            File file=new File(outputPath + groupNumber + ".csv");
            
            if(file.isFile() && file.exists()) { //判断文件是否存在	
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                
                int i = 0;
                while(((lineTxt = bufferedReader.readLine()) != null) && (i < times)){
                	
                	String[] splittedLine = formatLine(lineTxt);
                	String userId = splittedLine[0];
                	
                	if (!userIdArr.contains(userId)) {
                		userIdArr.add(userId);
                	} 	
                }
                read.close();
                
                return userIdArr;
		    }
            else {
		        System.out.println("找不到指定的文件");
		    }
	    } catch (Exception e) {
	        System.out.println("读取文件内容出错");
	        e.printStackTrace();
	    }  
    	
    	return null;
    }
    
    private void handleDataByGroup(int groupNumber, ArrayList<String> userIdArr) {
    	
    	ArrayList<LogRecord> logRecords = logRecordTable.get(groupNumber);
    	
    	for (String userId:userIdArr) {
        	
        	int time = 0;
        	String lastUrl = null;
        	String currentUrl = null;
            
            for (LogRecord record:logRecords) {
            	if (record.getUserId().equals(userId) && (time == 0)) {
            		lastUrl = record.getURL();
            	}
            	else if (record.getUserId().equals(userId) && (time != 0)) {
            		currentUrl = record.getURL();
            		updateDataTable(lastUrl, currentUrl);  // 把当前的记录加到dataTable中去
            		lastUrl = currentUrl;
            	}
            	time++;
            }
        }
    }
    
    private void updateDataTable(String lastUrl, String currentUrl) {
    	
    	ArrayList<ResourceInfo> urlArr = dataTable.get(lastUrl);
    	boolean isUpdated = false;
    	
    	if (!dataTable.containsKey(lastUrl)) {
    		dataTable.put(lastUrl, null);
    	}
    	
    	if (urlArr == null) {
			urlArr = new ArrayList<ResourceInfo>();
			ResourceInfo ri = new ResourceInfo("", 0);
			ri.setUrl(currentUrl);
			ri.setCount(1);
			urlArr.add(ri);
			dataTable.put(lastUrl, urlArr);
		}
		else {
			for (int i = 0; i < urlArr.size(); i++) {
				if ( urlArr.get(i).getUrl().equals(currentUrl)) {   //找到了该转移路径
					urlArr.get(i).countIncrement();
					isUpdated = true;
				}
			}
			
			if (!isUpdated) {   // 没找到该转移路径
				ResourceInfo ri = new ResourceInfo("", 0);
				ri.setUrl(currentUrl);
				ri.setCount(1);
				urlArr.add(ri);
				dataTable.replace(lastUrl, urlArr);
			}
		}
    }
    
    /*
     * 把整个日志文件中的数据按300秒一分隔，分别输出到csv文件中
     */
    private void parserWrite(int index, String line) {
    	BufferedWriter writer = null;
		try {	
		    writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(outputPath + index + ".csv", true)));

		    writer.append(line);
		    writer.newLine();
		  
		} catch (Exception ex) {
			
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
    }
    
    private void appendToLogRecordTable(int groupNumber, String lineTxt) {
    	
    	String[] splitedArr = formatLine(lineTxt);
    	LogRecord record = new LogRecord(splitedArr[0], splitedArr[1]);
    	
    	if (!logRecordTable.containsKey(groupNumber)) {
    		logRecordTable.put(groupNumber, null);
    	}
    	
    	ArrayList<LogRecord> records = logRecordTable.get(groupNumber);	
    	records.add(record);
    	logRecordTable.replace(groupNumber, records);
    }
    
    private int getTimeGroup(String lineTxt, int lineNum) {
    	
    	int group = -1;
    	if (lineNum == 0) {
    		return group;
    	}
    	
    	String[] splittedLine = formatLine(lineTxt);
        String hourMinSec = splittedLine[2].substring(11);
        
        int maxSplit = 3;
		String[] splitedArr = hourMinSec.split(":", maxSplit);
		
		int sum = Integer.parseInt(splitedArr[2]) + Integer.parseInt(splitedArr[1]) * 60 + Integer.parseInt(splitedArr[0]) * 3600;
		group = sum / interval;
		
        return group;
    }

	private String[] formatLine(String lineTxt) {
		int maxSplit = 4;
		String[] splitedArr = lineTxt.split(",", maxSplit);
		String[] splitedURL = splitedArr[1].split("/");
		splitedArr[1] = splitedURL[splitedURL.length - 1];
		
		return splitedArr;
	}
	
	public void writeDataToFile() {
		
		BufferedWriter writer = null;
		try {	
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(outputPath + "matrix.csv"), "utf-8"));
		    
		    int matrixSize = dataTable.keySet().size();
		    String firstRow = "";
		    
		    HashMap<String, Integer> urlToIndex = new HashMap<String, Integer>();
		    int mapIndex = 0;
		    for (String key:dataTable.keySet()) {
		    	firstRow += ( ";" + key);
		    	urlToIndex.put(key, mapIndex);
		    	mapIndex++;
		    }
		    
		    // 输出矩阵第一行
		    writer.write(firstRow);
		    writer.newLine();
		    
		    for (String key:dataTable.keySet()) {
		    	if (dataTable.get(key) == null) {
		    		String row = key;
		    		for (int i = 0; i < matrixSize; i++) {
		    			row += (";" + 0);
		    		}
		    		writer.write(row);
				    writer.newLine();
		    	}
		    	else {
		    		String row = key;
		    		int countSum = 0;
		    		boolean hasProbality = false;
		    		
		    		for (ResourceInfo info: dataTable.get(key)) {
		    			countSum += info.getCount();
		    		}
		    		for (String url: dataTable.keySet()) {
		    			hasProbality = false;
		    			for (ResourceInfo info: dataTable.get(key)) {
		    				if (url.equals(info.getUrl())) {
		    					double sum = countSum;
		    					double fraction = info.getCount() / sum;
		    					String formatedFraction = String.format("%.5f", fraction);
		    					row += (";" + formatedFraction);
		    					hasProbality = true;
		    					break;
		    				}
		    			}
		    			if (!hasProbality) {
		    				row += (";" + 0);
		    			}	
		    		}
		    		writer.write(row);
				    writer.newLine();
		    	}
		    }
		   
		} catch (Exception ex) {
			
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	public void printHashTable() {
		int urlNumber = 0;
		for (String key:dataTable.keySet()) {
			System.out.println("-" + key);
			ArrayList<ResourceInfo> resourceArr = dataTable.get(key);
			if (resourceArr != null) {
				for (int i = 0; i < resourceArr.size(); i++) {
					System.out.println("--" + resourceArr.get(i).getUrl() + ": " + resourceArr.get(i).getCount() + "次");
				}
			}
			urlNumber++;
		}
		
		System.out.println("一共有" + urlNumber + "种不同的资源类型");
	}

}
