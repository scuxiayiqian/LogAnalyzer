package readCSV;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

public class CsvReader {
			
	public double[][] transionMatrix;
	private String inputPath;
	private String outputPath;
	private int interval = 300;
	private int times = 0;
	private String groupNumber = "-1";
	private ArrayList<String> groupNumberArr = new ArrayList<String>();
	private HashMap<String, ArrayList<LogRecord>> logRecordTable = new HashMap<String, ArrayList<LogRecord>>();
	private HashMap<String, ArrayList<ResourceInfo>> dataTable = new HashMap<String, ArrayList<ResourceInfo>>();
	
	public void setTimes(int times) {
		this.times = times;
	}
	
	public int getTimes() {
		return this.times;
	}
	
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
                
                int i = 0;
                String preLineTxt = null;
                String lineTxt = null;
                boolean isNewDay = false;
                String curDateStr = null;
                String preDateStr = null;
                
                while(((lineTxt = bufferedReader.readLine()) != null) && (i < times)){
                	
                	if (i == 0) {
                		i++;
                		continue;
                	}
                	
                	constructDataTable(i, lineTxt, preLineTxt);  // 构造dataTable
                	
                	if (i > 0) {
                		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    	String[] splittedLine = this.formatLine(lineTxt);
                    	
                    	if (preLineTxt != null) {
                    		String[] presplittedLine = this.formatLine(preLineTxt);
                        	
                            try {
                    			Date curdate = df.parse(splittedLine[2]);
                    			Date predate = df.parse(presplittedLine[2]);
                    			
                    			int compare = curdate.compareTo(predate);
//                    			System.out.println(compare);
//                    			System.out.println("------");
                    			
                    			if (compare == 1) {
                    				isNewDay = true;
                    			}
                    			else if (compare == 0) {
                    				isNewDay = false;
                    			}
                    
                    			curDateStr = df.format(curdate);
                    			preDateStr = df.format(predate);
                    			
                    			groupNumber = getTimeGroup(lineTxt, preLineTxt, i);  
//                            	groupNumber = isNewDay? (curDateStr + "-" + groupNumber):(preDateStr + "-" + groupNumber);
                            	
                            	if (isNewDay) {
                            		groupNumber = curDateStr + "-" + groupNumber;
                            		preLineTxt = null;
                            	}
                            	else {
                            		groupNumber = preDateStr + "-" + groupNumber;
                            	}
                    		} catch (ParseException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                    	}
                    	else {
                    		try {
                    			Date curdate = df.parse(splittedLine[2]);
                    			curDateStr = df.format(curdate);
                    			
                    			groupNumber = getTimeGroup(lineTxt, preLineTxt, i);  
                            	groupNumber = curDateStr + "-" + groupNumber;
                    		} catch (ParseException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                    	}
                	} 	
                	
                	if (!groupNumberArr.contains(groupNumber)) {
                		groupNumberArr.add(groupNumber);
                	}
                	
                	parserWrite(groupNumber, lineTxt); // append行到对应group的csv文件中
                	appendToLogRecordTable(groupNumber, lineTxt);  // 将log以hash<array>的方式存入内存，以便之后分组处理的时候不用再去读文件
                	
                	i++;
                	preLineTxt = lineTxt;
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
    
    // 收集时间点timepoint之前和之后timeInterval时间段的url出现次数
    public void collectRSTimes(Date timepoint, long timeInterval, long intervalTimes, double[] weightArr) {
    	try {
            String encoding="GBK";
            File file=new File(inputPath);
            
            if(file.isFile() && file.exists()) { //判断文件是否存在	
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                
                int i = 0;
                String lineTxt = null;
                
                HashMap<String, Integer> trainingDataset = new HashMap<String, Integer>();
                HashMap<String, Integer> testingDataset = new HashMap<String, Integer>();
                
                while((lineTxt = bufferedReader.readLine()) != null){
                	
                	if (i == 0) {
                		i++;
                		continue;
                	}
                	                	
					if (i > 0) {
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						String[] splittedLine = this.formatLine(lineTxt);

						try {
							Date curdate = df.parse(splittedLine[2]);
							String rc = splittedLine[1];   //   url name
							
							int compare = curdate.compareTo(timepoint);
							long timeDistance = Math.abs(curdate.getTime() - timepoint.getTime());
							int lastInterval = -1;
							ArrayList<HashMap<String, Integer>> trainingDatasetArr = new ArrayList< HashMap<String, Integer> >();
							
							if (compare < 0 && timeDistance < (timeInterval * intervalTimes)) {
								int newInterval = (int) (timeDistance / timeInterval);
								System.out.println("interval: " + newInterval);
								if (lastInterval == -1) {
									if (trainingDataset.get(rc) == null) {
										trainingDataset.put(rc, 1);
									}
									else {
										trainingDataset.put(rc, (trainingDataset.get(rc) + 1));
									}
									lastInterval = newInterval;
								}
								else if (lastInterval == newInterval) {
									if (trainingDataset.get(rc) == null) {
										trainingDataset.put(rc, 1);
									}
									else {
										trainingDataset.put(rc, (trainingDataset.get(rc) + 1));
									}
									lastInterval = newInterval;
								}
								else if (lastInterval != newInterval) {
									trainingDatasetArr.add(trainingDataset);
									trainingDataset.clear();
									
									if (trainingDataset.get(rc) == null) {
										trainingDataset.put(rc, 1);
									}
									else {
										trainingDataset.put(rc, (trainingDataset.get(rc) + 1));
									}
									lastInterval = newInterval;
								}
							}
							else if (compare > 0 && timeDistance < timeInterval) {
								if (testingDataset.get(rc) == null) {
									testingDataset.put(rc, 1);
								}
								else {
									testingDataset.put(rc, (testingDataset.get(rc) + 1));
								}
							}
							else if (compare > 0 && timeDistance > timeInterval) {
								
								ArrayList<HashMap<String, Integer>> finalTrainingMapArr = getPredictedResourceTimeArr (
										trainingDatasetArr, 
										weightArr, 
										weightArr.length);
								
								HashMap<String, Integer> finalTrainingSet = new HashMap<String, Integer>();
								for (HashMap<String, Integer> urlMap:finalTrainingMapArr) {
									int index = finalTrainingMapArr.indexOf(urlMap);
									for (String rsName:urlMap.keySet()) {
										if (finalTrainingSet.get(rsName) == null) {
											int weightedTimes = (int) (urlMap.get(rsName) * weightArr[index]);
											finalTrainingSet.put(rsName, weightedTimes);
										}
										else {
											int weightedTimes = (int) (urlMap.get(rsName) * weightArr[index]) + finalTrainingSet.get(rsName);
											finalTrainingSet.put(rsName, weightedTimes);
										}
									}
								}
								
								String[] rsArr = new String[finalTrainingSet.size()];
								Integer[] predictedTimes = new Integer[finalTrainingSet.size()];
								int idx = 0;
								for (String rs:finalTrainingSet.keySet()) {
									rsArr[idx] = rs;
									predictedTimes[idx] = finalTrainingSet.get(rs);
									idx++;
								}
								
								XlsWriter xlsWriter = new XlsWriter(outputPath + "finalPrediction.xls");
								xlsWriter.exportToExcel(testingDataset, predictedTimes, rsArr);
								break;
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
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


	public ArrayList<HashMap<String, Integer>> getPredictedResourceTimeArr (
			ArrayList<HashMap<String, Integer>> trainingDatasetArr, 
			double[] weightArr, 
			long intervalTimes ) {
		
		ArrayList<HashMap<String, Integer>> predictedRSArr = new ArrayList<HashMap<String, Integer>>();
		if ( weightArr.length != intervalTimes) {
			System.out.println("weight array's length not equal to interval times!");
			System.out.println("weightArr length: " + weightArr.length);
			System.out.println("interval times: " + intervalTimes);
			
			System.exit(0);
		}
		
		for (int i = 0; i < trainingDatasetArr.size(); i++) {
			String[] rsArr = new String[trainingDatasetArr.get(i).size()];
			Integer[] timeArr = new Integer[trainingDatasetArr.get(i).size()];
			int idx = 0;
			for (String rs:trainingDatasetArr.get(i).keySet()) {
				rsArr[idx] = rs;
				timeArr[idx] = trainingDatasetArr.get(i).get(rs);
				idx++;
			}
	 		double[][] predictedMatrix = producePartlyTransitionMatrix(rsArr);
	 		Integer[] finaltimes = new Integer[rsArr.length];
	 		Integer[] tmpTimes = timeArr;
	 		
	 		for (int k = 0; k < intervalTimes; k++) {
	 			for (int p = 0; p < rsArr.length; p++) {
	 				int sum = 0;
	 				for (int j = 0; j < rsArr.length; j++) {
	 					sum += tmpTimes[j] * predictedMatrix[j][p];
	 				}
	 				finaltimes[p] = sum;
	 			}
	 			tmpTimes = finaltimes;
	 		}
	 		
	 		HashMap<String, Integer> map = new HashMap<String, Integer>();
	 		int index = 0;
	 		for (String rs: rsArr) {
	 			map.put(rs, finaltimes[index]);
	 			index++;
	 		}
	 		predictedRSArr.add(map);
	 		intervalTimes--;
		}
	
		return predictedRSArr;
    }

    // markovPrediction 
// 	public Integer[] getPredictedResourceTime(Integer[] tmArr, String[] rsArr, int predictionTimes) {
// 		
// 		double[][] predictedMatrix = producePartlyTransitionMatrix(rsArr);
// 		Integer[] finaltimes = new Integer[rsArr.length];
// 		Integer[] tmpTimes = tmArr;
// 		
// 		for (int k = 0; k < predictionTimes; k++) {
// 			for (int i = 0; i < rsArr.length; i++) {
// 				int sum = 0;
// 				for (int j = 0; j < rsArr.length; j++) {
// 					sum += tmpTimes[j] * predictedMatrix[j][i];
// 				}
// 				finaltimes[i] = sum;
// 			}
// 			tmpTimes = finaltimes;
// 		}
// 		
// 		System.out.println("-----资源名");
// 		for (String rs:rsArr) {
// 			System.out.print(rs + ", ");
// 		}
// 		
// 		System.out.println("-----资源对应的真实出现次数");
// 		for (int tm:tmArr) {
// 			System.out.print(tm + ", ");
// 		}
// 		
// 		System.out.println("-----对应转移矩阵");
// 		printTransitionMatrix(predictedMatrix);
// 		
// 		System.out.println("-----源对应的预测出现次数");
// 		for (int ft:finaltimes) {
// 			System.out.print(ft + ", ");
// 		}
// 		
// 		return finaltimes;
// 	}
    
    public void handleDatas() {
    	for (String groupNum:groupNumberArr) {
    		ArrayList<String> userIdArr = new ArrayList<String>();
    		userIdArr = getAllUserId(groupNum);
    		handleDataByGroup(groupNum, userIdArr);
    	}
    }
    
    private void constructDataTable(int index, String line, String preLine)  {
    	String[] formattedLine = formatLine(line);
		String url = formattedLine[1];
		
		if (index == 1) {
			dataTable.put(url, null);
		} 
		else if (index > 1) {
			// 如果没有key，就在table中加一个以此行url为key的记录
			if (!dataTable.containsKey(url)) {
				dataTable.put(url, null);
			}
		}
    }
    
    private ArrayList<String> getAllUserId(String groupNumber) {
    	
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
    
    private void handleDataByGroup(String groupNumber, ArrayList<String> userIdArr) {
    	
    	ArrayList<LogRecord> logRecords = logRecordTable.get(groupNumber);
    	
    	for (String userId:userIdArr) {
        	
        	boolean firstOneNotFound = true;
        	String lastUrl = null;
        	String currentUrl = null;
            
            for (LogRecord record:logRecords) {
            	if (record.getUserId().equals(userId) && (firstOneNotFound)) {
            		lastUrl = record.getURL();
            		firstOneNotFound = false;
            	}
            	else if (record.getUserId().equals(userId) && (!firstOneNotFound)) {
            		currentUrl = record.getURL();
            		updateDataTable(lastUrl, currentUrl);  // 把当前的记录加到dataTable中去
            		lastUrl = currentUrl;
            	}
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
    private void parserWrite(String index, String line) {
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
    
    private void appendToLogRecordTable(String groupNumber, String lineTxt) {
    	
    	String[] splitedArr = formatLine(lineTxt);
    	LogRecord record = new LogRecord(splitedArr[0], splitedArr[1]);
    	
    	if (!logRecordTable.containsKey(groupNumber)) {
    		logRecordTable.put(groupNumber, null);
    	}
    	
    	ArrayList<LogRecord> records = logRecordTable.get(groupNumber);	    	
    	if (records == null) {
    		records = new ArrayList<LogRecord>();
			LogRecord rc = new LogRecord(splitedArr[0], splitedArr[1]);
			records.add(rc);
			logRecordTable.put(groupNumber, records);
    	}
    	else {
        	records.add(record);
        	logRecordTable.replace(groupNumber, records);
    	}
    }
    
    private String getTimeGroup(String lineTxt, String preLineTxt, int lineNum) {
    	
    	String[] splittedLine = formatLine(lineTxt);
        String hourMinSec = splittedLine[2].substring(11);
    	
    	String group = "-1";
    	if (lineNum == 0) {
    		return group;
    	}

        int maxSplit = 3;
		String[] splitedArr = hourMinSec.split(":", maxSplit);
		
		int sum = Integer.parseInt(splitedArr[2]) + Integer.parseInt(splitedArr[1]) * 60 + Integer.parseInt(splitedArr[0]) * 3600;
		Integer groupnum = sum / interval;
		group = groupnum.toString();
		
        return group;
    }

	private String[] formatLine(String lineTxt) {
		int maxSplit = 4;
		String[] splitedArr = lineTxt.split(",", maxSplit);
		String[] splitedURL = splitedArr[1].split("/");
		splitedArr[1] = splitedURL[splitedURL.length - 1];
		
		return splitedArr;
	}
	
	public void writeMatrixToFile() {
		
		int matrixSize = dataTable.keySet().size();
	    transionMatrix = new double[matrixSize][matrixSize];
		BufferedWriter writer = null;
		
		try {	
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(outputPath + "matrix.csv"), "utf-8"));
		    
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
		    
		    int rowIndex = 0;
		    for (String key:dataTable.keySet()) {
		    	if (dataTable.get(key) == null) {
		    		String row = key;
		    		for (int i = 0; i < matrixSize; i++) {
		    			row += (";" + 0);
		    			transionMatrix[rowIndex][i] = 0;
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
		    		int colIndex = 0;
		    		for (String url: dataTable.keySet()) {
		    			hasProbality = false;
		    			
		    			for (ResourceInfo info: dataTable.get(key)) {
		    				if (url.equals(info.getUrl())) {
		    					double sum = countSum;
		    					double fraction = info.getCount() / sum;
		    					String formatedFraction = String.format("%.5f", fraction);
		    					row += (";" + formatedFraction);
		    					
		    					transionMatrix[rowIndex][colIndex] = fraction;
		    					
		    					hasProbality = true;
		    					break;
		    				}
		    			}
		    			if (!hasProbality) {
		    				row += (";" + 0);
		    				transionMatrix[rowIndex][colIndex] = 0;
		    			}	
		    			colIndex++;
		    		}
		    		writer.write(row);
				    writer.newLine();
		    	}
		    	rowIndex++;
		    }
		   
		} catch (Exception ex) {
			
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	public double[][] producePartlyTransitionMatrix(String[] resourceArr) {
		
		HashMap<String, ArrayList<ResourceInfo>> partlyDataTable = new HashMap<String, ArrayList<ResourceInfo>>();
		
		// 从dataTable中把resourceArr挑出来，把不属于resourceArr中的url删掉
		for (String resource:resourceArr) {
			ArrayList<ResourceInfo> resourceInfos = dataTable.get(resource);
			ArrayList<ResourceInfo> partlyResourceInfos = new ArrayList<ResourceInfo>();
			
			if (resourceInfos == null) {
				partlyDataTable.put(resource, null);
			}
			else {
				for (String singleresource:resourceArr) {
					for (ResourceInfo info:resourceInfos) {
						if (info.getUrl().equals(singleresource)) {
							partlyResourceInfos.add(info);
						}
					}
				}
				partlyDataTable.put(resource, partlyResourceInfos);
			}
		}
		
		// 生成新的部分转移矩阵
		int matrixSize = partlyDataTable.keySet().size();
		double[][] partlyTransitionMatrix = new double[matrixSize][matrixSize];
		
		int rowIndex = 0;
		for (String key:partlyDataTable.keySet()) {
	    	if (partlyDataTable.get(key) == null) {
	    		for (int i = 0; i < matrixSize; i++) {
	    			partlyTransitionMatrix[rowIndex][i] = 0;
	    		}
	    	}
	    	else {
	    		int countSum = 0;
	    		boolean hasProbality = false;
	    		
	    		for (ResourceInfo info: partlyDataTable.get(key)) {
	    			countSum += info.getCount();
	    		}
	    		int colIndex = 0;
	    		for (String url: partlyDataTable.keySet()) {
	    			hasProbality = false;
	    			
	    			for (ResourceInfo info: partlyDataTable.get(key)) {
	    				if (url.equals(info.getUrl())) {
	    					double sum = countSum;
	    					double fraction = info.getCount() / sum;
	    					partlyTransitionMatrix[rowIndex][colIndex] = fraction;
	    					
	    					hasProbality = true;
	    					break;
	    				}
	    			}
	    			if (!hasProbality) {
	    				partlyTransitionMatrix[rowIndex][colIndex] = 0;
	    			}	
	    			colIndex++;
	    		}
	    		
	    	}
	    	rowIndex++;
	    }
		
		return partlyTransitionMatrix;
	}
	
	public void printTransitionMatrix(double[][] matrix) {
		System.out.println("-------- bewlow is the probability transition matrix --------");
		for(double[] numArr:matrix) {
			for (double num:numArr) {
				System.out.print(num + " ");
			}
			System.out.println();
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
