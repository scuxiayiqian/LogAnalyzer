package com.github.scuxiayiqian;

public class EntryPoint {
    public static void main(String[] args) {
        CsvReader csvReader;
        if (args.length == 0)
            csvReader = new CsvReader("/Users/xiayiqian/Downloads/data.csv");
        else
            csvReader = new CsvReader(args[0]);
        csvReader.read();
        csvReader.print();
    }
}
