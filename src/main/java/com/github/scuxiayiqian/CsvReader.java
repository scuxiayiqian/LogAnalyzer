package com.github.scuxiayiqian;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CsvReader {

    private String inputPath;
    private String outputPath;
    private String encoding;
    private Map<UrlTransit, Integer> urlTransitIntegerMap = new HashMap<UrlTransit, Integer>();
    private Map<String, Integer> urlMap = new HashMap<String, Integer>();
    private List<String> urlNames = new ArrayList<String>();

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

    public void read() throws IOException {
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
    }

    private LogRecord handleLine(LogRecord lastLogRecord, String line) {
        LogRecord currentLogRecord = extractInfo(line);
        if ((lastLogRecord != null) && (lastLogRecord.getUserId().equals(currentLogRecord.getUserId()))) {
            UrlTransit urlTransit = new UrlTransit(lastLogRecord.getUrl(), currentLogRecord.getUrl());
            setUrlMapping(urlTransit.getSource());
            setUrlMapping(urlTransit.getTarget());
            Integer value = urlTransitIntegerMap.get(urlTransit);
            int candidate = value != null ? value : 0;
            urlTransitIntegerMap.put(urlTransit, candidate + 1);
        }
        return currentLogRecord;
    }

    private void setUrlMapping(String string) {
        if (!urlMap.containsKey(string)) {
            urlNames.add(string);
            urlMap.put(string, urlNames.size() - 1);
        }
    }

    public void print() throws FileNotFoundException, UnsupportedEncodingException {
        double[][] matrix = calcMatrix();
        PrintWriter writer = new PrintWriter(outputPath, encoding);
        for (String urlName : urlNames) writer.print(String.format(";%s", urlName));
        writer.println();
        for (int target = 0; target < urlNames.size(); target++) {
            writer.print(urlNames.get(target));
            for (int source = 0; source < urlNames.size(); source++)
                writer.print(String.format(";%.5f", matrix[source][target]));
            writer.println();
        }
        writer.close();
    }

    private double[][] calcMatrix() {
        double[][] matrix = new double[urlNames.size()][urlNames.size()];
        for (int source = 0; source < urlNames.size(); source++) {
            int numberOfRecords = 0;
            for (String urlName : urlNames) {
                UrlTransit candidate = new UrlTransit(urlNames.get(source), urlName);
                if (urlTransitIntegerMap.containsKey(candidate))
                    numberOfRecords += urlTransitIntegerMap.get(candidate);
            }
            for (int target = 0; target < urlNames.size(); target++) {
                UrlTransit candidate = new UrlTransit(urlNames.get(source), urlNames.get(target));
                if (urlTransitIntegerMap.containsKey(candidate))
                    matrix[source][target] = urlTransitIntegerMap.get(candidate) / numberOfRecords;
            }
        }
        return matrix;
    }
}
