package readCSV;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

public class CsvReader {
	
	private String inputPath;
	private String outputPath;
	
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
    
    public void readAndWrite() {
    	try {
            String encoding="GBK";
            File file=new File(inputPath);
            int i = 0;
            if(file.isFile() && file.exists()) { //判断文件是否存在	
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                String preLineTxt = null;
                int j = 0;
                while(((lineTxt = bufferedReader.readLine()) != null) && (j < 100000)){
                	handleLine(i, lineTxt, preLineTxt);
                	preLineTxt = lineTxt;
                	i++;
                	j++;
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

    public void handleLine(int index, String line, String preLine) 
	{	
		String[] formattedLine = formatLine(line);
		String url = formattedLine[1];
		boolean isUpdated = false;
		
		if (index == 1) {
			dataTable.put(url, null);
		} 
		else if (index > 1) {
			// 如果没有key，就在table中加一个以此行url为key的记录
			if (!dataTable.containsKey(url)) {
				dataTable.put(url, null);
			}
			
			String[] preFormattedLine = formatLine(preLine);
			String preUrl = preFormattedLine[1];
			
			// 如果userid相同，就把当前的url加到前一个url对应的value中
			if (preFormattedLine[0].equals(formattedLine[0])) {
				ArrayList<ResourceInfo> urlArr = dataTable.get(preUrl);
				
				if (urlArr == null) {
					urlArr = new ArrayList<ResourceInfo>();
					ResourceInfo ri = new ResourceInfo("", 0);
					ri.setUrl(url);
					ri.setCount(1);
					urlArr.add(ri);
					dataTable.put(preUrl, urlArr);
				}
				else {
					for (int i = 0; i < urlArr.size(); i++) {
						if ( urlArr.get(i).getUrl().equals(url)) {
							urlArr.get(i).countIncrement();
							isUpdated = true;
						}
					}
					
					if (!isUpdated) {
						ResourceInfo ri = new ResourceInfo("", 0);
						ri.setUrl(url);
						ri.setCount(1);
						urlArr.add(ri);
						dataTable.replace(preUrl, urlArr);
					}
				}
			}
		}
	}
	
	public String[] formatLine(String lineTxt) {
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
		          new FileOutputStream(outputPath), "utf-8"));
		    
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