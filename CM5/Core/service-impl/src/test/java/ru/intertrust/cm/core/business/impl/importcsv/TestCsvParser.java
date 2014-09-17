package ru.intertrust.cm.core.business.impl.importcsv;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

//import au.com.bytecode.opencsv.CSVReader;

public class TestCsvParser {
    private List<String> etalon = new ArrayList<String>();

    @Before
    public void init() {
        etalon.add("field 1");
        etalon.add("field2");
        etalon.add("field 3");
        etalon.add("По русски");
        etalon.add("c пробелами");
        etalon.add("Несколько строк\r\nвторая строка");
        etalon.add("содержащий точку ; с запятой");
        etalon.add("содержит, запятую");
        etalon.add("содержит \" кавычки");
        etalon.add("содержит ' апостроф");
        etalon.add("   содержащий пробелы по краям    ");
        etalon.add("содержит обратный\\слэш");
        etalon.add("содержит прямой/слэш");
    }

    /*@Test
    public void testOpenCsv() throws Exception {
      try (CSVReader reader = new CSVReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("test.csv"), "ANSI-1251"), ';', '"')) {
            String[] nextLine;
            int j = 0;
            while ((nextLine = reader.readNext()) != null) {
                for (int i = 0; i < nextLine.length; i++) {
                    if (nextLine[i] != null && !nextLine[i].isEmpty()) {
                        assertTrue(etalon.get(j) + " != " + nextLine[i], etalon.get(j).equals(nextLine[i]));
                        j++;
                    }
                }
            }
        }
    }*/

    @Test
    public void testApacheCsv() throws Exception {
        Reader in = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("test.csv"), "ANSI-1251");
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').parse(in);
        int j = 0;
        for (CSVRecord record : records) {
            for (int i = 0; i < record.size(); i++) {
                if (record.get(i) != null && !record.get(i).isEmpty()) {
                    assertTrue(etalon.get(j) + " != " + record.get(i), etalon.get(j).equals(record.get(i)));
                    j++;
                }
            }
        }
    }
}
