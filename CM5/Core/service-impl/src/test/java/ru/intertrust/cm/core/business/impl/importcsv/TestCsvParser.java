package ru.intertrust.cm.core.business.impl.importcsv;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

public class TestCsvParser {
    
    @Test
    public void testOpenCsv() throws Exception {
        List<String> etalon = new ArrayList<String>();
        etalon.add("field 1");
        etalon.add("field2");
        etalon.add("field 3");
        etalon.add("По русски");
        etalon.add("c пробелами");
        etalon.add("Несколько строк\nвторая строка");
        etalon.add("содержащий точку ; с запятой");
        etalon.add("содержит, запятую");
        etalon.add("содержит \" кавычки");
        etalon.add("содержит ' апостроф");
        etalon.add("   содержащий пробелы по краям    ");

        CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("test.csv"), "ANSI-1251"), ';', '"');
        String [] nextLine;
        int j = 0;
        while ((nextLine = reader.readNext()) != null) {
            for (int i = 0; i < nextLine.length; i++) {
                j++;
            }
        }        
    }
    
    
}
