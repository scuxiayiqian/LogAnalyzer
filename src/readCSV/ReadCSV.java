package readCSV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.ArrayList;

public class ReadCSV {

	static HashMap<String, ArrayList<ResourceInfo>> dataTable = new HashMap<String, ArrayList<ResourceInfo>>();
	
	public static void main(String args[])
	{
		try {
            String encoding="GBK";
            File file=new File("/Users/xiayiqian/Downloads/data.csv");
            int i = 0;
            if(file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                String preLineTxt = null;
                int j = 0;
                while(((lineTxt = bufferedReader.readLine()) != null) && (j < 100)){
                	handleLine(i, lineTxt, preLineTxt);
                	preLineTxt = lineTxt;
                	i++;
                	j++;
                }
                printHashTable(dataTable);
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
	
	public static void handleLine(int index, String line, String preLine) 
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
	
	public static String[] formatLine(String lineTxt) {
		int maxSplit = 4;
		String[] splitedArr = lineTxt.split(",", maxSplit);
		String[] splitedURL = splitedArr[1].split("/");
		splitedArr[1] = splitedURL[splitedURL.length - 1];
		
		return splitedArr;
	}
	
	public static void printHashTable (HashMap<String, ArrayList<ResourceInfo>> dataTable) {
		for (String key:dataTable.keySet()) {
			System.out.println("-" + key);
			ArrayList<ResourceInfo> resourceArr = dataTable.get(key);
			if (resourceArr != null) {
				for (int i = 0; i < resourceArr.size(); i++) {
					System.out.println("--" + resourceArr.get(i).getUrl() + ": " + resourceArr.get(i).getCount() + "次");
				}
			}
		}
	}
}
