package com.github.scuxiayiqian;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EntryPoint {
    public static void main(String[] args) {
        CsvReader csvReader;
        if (args.length == 0)
            csvReader = new CsvReader("/Users/xiayiqian/Downloads/data.csv", "/Users/xiayiqian/Downloads/output.csv");
        else if (args.length == 1)
            csvReader = new CsvReader(args[0], "/Users/xiayiqian/Downloads/output.csv");
        else
            csvReader = new CsvReader(args[0], args[1]);
        try {
            csvReader.read();
            csvReader.print();
        } catch (FileNotFoundException ex) {
            System.out.println("找不到指定的文件");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("读取文件内容出错");
            ex.printStackTrace();
        }
    }
}
