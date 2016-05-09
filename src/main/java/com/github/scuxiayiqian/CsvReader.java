package com.github.scuxiayiqian;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class CsvReader {

    private String inputPath;
    private String outputPath;
    private String encoding;
    private Map<UrlTransit, Integer> urlTransitIntegerMap = new HashMap<UrlTransit, Integer>();
    private int[][] matrix;
    private Map<String, Integer> urlMap = new HashMap<String, Integer>();
    public CsvReader(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    private static String GetEncoding(String path) throws IOException {
        return UniversalDetector.detectCharset(new File(path));
    }

    private static LogRecord extractInfo(String lineTxt) {
        int maxSplit = 4;
        String[] splitArr = lineTxt.split(",", maxSplit);
        String[] splitUrl = splitArr[1].split("/");
        return new LogRecord(splitArr[0], splitUrl[splitUrl.length - 1]);
    }

    public void read() {
        try {
            encoding = GetEncoding(inputPath);
            if (encoding == null)
                encoding = "UTF-8";
            File file = new File(inputPath);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String lineTxt;
            LogRecord lastLogRecord = null;
            int count = 0;
            while (((lineTxt = bufferedReader.readLine()) != null) && (++count < 10000)) {
                lastLogRecord = handleLine(lastLogRecord, lineTxt);
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

    private LogRecord handleLine(LogRecord lastLogRecord, String line) {
        LogRecord currentLogRecord = extractInfo(line);
        if ((lastLogRecord != null) && (lastLogRecord.getUserId().equals(currentLogRecord.getUserId()))) {
            UrlTransit urlTransit = new UrlTransit(lastLogRecord.getUrl(), currentLogRecord.getUrl());
            Integer value = urlTransitIntegerMap.get(urlTransit);
            int candidate = value != null ? value : 0;
            urlTransitIntegerMap.put(urlTransit, candidate + 1);
        }
        return currentLogRecord;
    }

    public void print() {
        try {
            File file = new File(outputPath);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            String lineTxt;
            LogRecord lastLogRecord = null;
            int count = 0;
            writer.close();
            bufferedWriter.close();
        } catch (FileNotFoundException ex) {
            System.out.println("找不到指定的文件");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("读取文件内容出错");
            ex.printStackTrace();
        }
    }
}
