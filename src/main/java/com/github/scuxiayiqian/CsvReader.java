package com.github.scuxiayiqian;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class CsvReader {

    private String path;
    private Map<ResourceInfo, Integer> dataTable = new HashMap();

    CsvReader(String path) {
        this.path = path;
    }

    private static String GetEncoding(String path) throws IOException {
        return UniversalDetector.detectCharset(new File(path));
    }

    private static ResourceInfo extractInfo(String lineTxt) {
        int maxSplit = 4;
        String[] splitArr = lineTxt.split(",", maxSplit);
        String[] splitUrl = splitArr[1].split("/");
        return new ResourceInfo(splitArr[0], splitUrl[splitUrl.length - 1]);
    }

    public void read() {
        try {
            String encoding = GetEncoding(path);
            if (encoding == null)
                encoding = "GBK";
            File file = new File(path);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(reader);
            String lineTxt;
            int count = 0;
            while (((lineTxt = bufferedReader.readLine()) != null) && (++count < 100)) {
                handleLine(lineTxt);
            }
            reader.close();
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("找不到指定的文件");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("读取文件内容出错");
            ex.printStackTrace();
        }
    }

    private void handleLine(String line) {
        ResourceInfo resourceInfo = extractInfo(line);

        Integer value = dataTable.get(resourceInfo);
        int candidate = value != null ? value : 0;
        dataTable.put(resourceInfo, candidate + 1);
    }

    public void print() {
        for (ResourceInfo resourceInfo : dataTable.keySet()) {
            System.out.println(String.format("%s-%s：%d次", resourceInfo.getUrl(), resourceInfo.getUserId(), dataTable.get(resourceInfo)));
        }
    }
}
