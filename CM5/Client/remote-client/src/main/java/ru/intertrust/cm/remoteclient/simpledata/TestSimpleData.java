package ru.intertrust.cm.remoteclient.simpledata;

import ru.intertrust.cm.core.business.api.SimpeDataStorage;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.simpledata.EqualSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.LikeSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.SimpleData;
import ru.intertrust.cm.core.business.api.simpledata.SimpleSearchOrder;
import ru.intertrust.cm.core.business.api.simpledata.SumpleSearchOrderDirection;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.*;

public class TestSimpleData extends ClientBase {

    public static void main(String[] args) {
        try {
            TestSimpleData test = new TestSimpleData();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            log("Start");

            SimpeDataStorage storage = getService("SimpeDataStorage", SimpeDataStorage.Remote.class);

            // Создаем 2 объекта
            SimpleData data = new SimpleData("test-simple-data");
            data.setString("test-string-single", "xxx");
            data.setString("test-string", "xxx", "yyy", "zzz");
            data.setBoolean("test-boolean", true, false);
            data.setLong("test-long", 10, 20);
            data.setDateTime("test-date-time", new Date(), new Date(), new Date());
            data.setTimelessDate("test-date", new TimelessDate(2020, 5,9), new TimelessDate(2020, 6,10));
            storage.save(data);

            SimpleData data2 = new SimpleData("test-simple-data");
            data2.setString("test-string-single", "yyy");
            data2.setString("test-string", "xxx", "yyy", "zzz");
            data2.setBoolean("test-boolean", true, false);
            data2.setLong("test-long", 10, 20);
            data2.setDateTime("test-date-time", new Date(), new Date(), new Date());
            data2.setTimelessDate("test-date", new TimelessDate(2020, 5,9), new TimelessDate(2020, 6,10));
            storage.save(data2);

            // Ищем фильтрами
            List<SimpleData> result = storage.find("test-simple-data",
                    Collections.singletonList(new EqualSimpleDataSearchFilter("test-string", new StringValue("xxx"))),
                    Arrays.asList("test-string", "test-long", "test-boolean", "test-date-time", "test-date"),
                    null);
            assertTrue("Find equal count", result.size() > 0);

            SimpleData findData = null;
            for (SimpleData row : result) {
                if (row.getId().equals(data.getId())){
                    findData = row;
                }
            }

            // Проверяем соответствие полей
            assertTrue("Doc with id in result", findData != null);
            assertTrue("Check multivalue str field 0", findData.getString("test-string").get(0).equals("xxx"));
            assertTrue("Check multivalue str field 1", findData.getString("test-string").get(1).equals("yyy"));
            assertTrue("Check multivalue str field 2", findData.getString("test-string").get(2).equals("zzz"));
            assertTrue("Check multivalue long field 0", findData.getLong("test-long").get(0).equals(10L));
            assertTrue("Check multivalue long field 1", findData.getLong("test-long").get(1).equals(20L));
            assertTrue("Check multivalue boolean field 0", findData.getBoolean("test-boolean").get(0).equals(true));
            assertTrue("Check multivalue boolean field 1", findData.getBoolean("test-boolean").get(1).equals(false));
            assertTrue("Check multivalue datetime field 0", findData.getDateTime("test-date-time").get(0).equals(data.getDateTime("test-date-time").get(0)));
            assertTrue("Check multivalue datetime field 1", findData.getDateTime("test-date-time").get(1).equals(data.getDateTime("test-date-time").get(1)));
            assertTrue("Check multivalue datetime field 2", findData.getDateTime("test-date-time").get(2).equals(data.getDateTime("test-date-time").get(2)));
            assertTrue("Check multivalue date field 0", findData.getTimelessDate("test-date").get(0).equals(data.getTimelessDate("test-date").get(0)));
            assertTrue("Check multivalue date field 1", findData.getTimelessDate("test-date").get(1).equals(data.getTimelessDate("test-date").get(1)));

            // Неправильный фильтр, ничего не должны найти
            result = storage.find("test-simple-data",
                    Collections.singletonList(new EqualSimpleDataSearchFilter("test-string", new StringValue("x"))),
                    null,
                    null);
            assertTrue("Find incorrect equal count", result.size() == 0);

            // Like фильтр
            result = storage.find("test-simple-data",
                    Collections.singletonList(new LikeSimpleDataSearchFilter("test-string", new StringValue("x"))),
                    null,
                    null);
            assertTrue("Find like count", result.size() > 0);

            // Ищем по ID
            findData = storage.find("test-simple-data", data.getId());
            assertTrue("Find by id", findData.getId().equals(data.getId()));
            assertTrue("Find by id string fields", findData.getString("test-string") != null);
            assertTrue("Find by id long fields", findData.getString("test-long") != null);
            assertTrue("Find by id boolean fields", findData.getString("test-boolean") != null);
            assertTrue("Find by id date fields", findData.getString("test-date") != null);
            assertTrue("Find by id datetime fields", findData.getString("test-date-time") != null);

            // Сортировка asc
            result = storage.find("test-simple-data", null, Arrays.asList("test-string-single"),
                    Collections.singletonList(new SimpleSearchOrder("test-string-single", SumpleSearchOrderDirection.ASC)));
            assertTrue("Check order asc", result.get(0).getString("test-string-single").get(0).equals("xxx"));

            // Сортировка desc
            result = storage.find("test-simple-data", null, Arrays.asList("test-string-single"),
                    Collections.singletonList(new SimpleSearchOrder("test-string-single", SumpleSearchOrderDirection.DESС)));
            assertTrue("Check order desc", result.get(0).getString("test-string-single").get(0).equals("yyy"));

            // лимит
            result = storage.find("test-simple-data", null, null, null, 1);
            assertTrue("Check limit", result.size() == 1);

            // Удаление по ID
            storage.delete(data.getId());
            findData = storage.find("test-simple-data", data.getId());
            assertTrue("Delete by id", findData == null);

            // Удаление по фильтру
            storage.delete("test-simple-data", null);

            result = storage.find("test-simple-data", null, null, null);
            assertTrue("Check delete by filters", result.size() == 0);

            if (hasError){
                log("Test ERROR");
            }else{
                log("Test COMPLETE");
            }
        } finally {
            writeLog();
        }
    }
}
