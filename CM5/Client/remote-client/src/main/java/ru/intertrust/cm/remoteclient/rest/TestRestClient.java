package ru.intertrust.cm.remoteclient.rest;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.restclient.client.CollectionRow;
import ru.intertrust.cm.core.restclient.client.DomainObject;
import ru.intertrust.cm.core.restclient.client.PlatformClient;
import ru.intertrust.cm.core.restclient.model.DomainObjectData;
import ru.intertrust.cm.core.restclient.model.ValueData;
import ru.intertrust.cm.core.restclient.model.WorkflowTaskAddressee;
import ru.intertrust.cm.core.restclient.model.WorkflowTaskData;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestRestClient {
    private static final Logger logger = LoggerFactory.getLogger(TestRestClient.class);
    private Random rnd = new Random();

    public static void main(String[] args) {
        TestRestClient test = new TestRestClient();
        test.execute();
    }

    public void execute(){
        try {
            PlatformClient client = new PlatformClient("http://localhost:8080/cm-sochi", "admin", "admin");

            String testString = "S_" + System.currentTimeMillis();
            String testText = "T_" + System.currentTimeMillis();
            Boolean testBoolean = rnd.nextBoolean();
            LocalDateTime testDateTime = LocalDateTime.now();
            LocalDate testDate = LocalDate.now();
            ZonedDateTime testZonedDateTime = ZonedDateTime.now();
            Double testDecimal = rnd.nextDouble();
            Long testLong = rnd.nextLong();

            // test save
            DomainObject type41 = client.create("test_type_41");
            type41.setValue("stringField", testString);
            type41 = client.save(type41);
            assert(testString.equals(type41.getValue("stringField")));

            String testReference = type41.toDomainObjectData().getId();

            // test find
            DomainObject findDomainObject = client.find(testReference);
            assert(findDomainObject.getId().equals(testReference));
            assert(findDomainObject.getValue("stringField").equals(testString));

            // Test save
            DomainObject type42 = client.create("test_type_42");
            type42.setValue("stringField", testString);
            type42.setValue("booleanField", testBoolean);
            type42.setValue("dateTimeField", testDateTime);
            type42.setValue("dateTimeWithTimeZoneField", testZonedDateTime);
            type42.setValue("decimalField", testDecimal);
            type42.setValue("longField", testLong);
            type42.setValue("referenceField", testReference);
            type42.setValue("textField", testText);
            type42.setValue("timelessDateField", testDate);

            type42 = client.save(type42);
            assert(testString.equals(type42.getValue("stringField")));
            assert(testBoolean.equals(type42.getValue("booleanField")));
            assert(testDateTime.equals(type42.getValue("dateTimeField")));
            assert(testZonedDateTime.equals(type42.getValue("dateTimeWithTimeZoneField")));
            assert(testDecimal.equals(type42.getValue("decimalField")));
            assert(testLong.equals(type42.getValue("longField")));
            assert(testReference.equals(type42.getValue("referenceField")));
            assert(testText.equals(type42.getValue("textField")));
            assert(testDate.equals(type42.getValue("timelessDateField")));

            // test saveAll
            DomainObject type42_2 = client.create("test_type_42");
            type42_2.setValue("stringField", testString);
            type42_2.setValue("booleanField", testBoolean);
            type42_2.setValue("dateTimeField", testDateTime);
            type42_2.setValue("dateTimeWithTimeZoneField", testZonedDateTime);
            type42_2.setValue("decimalField", testDecimal);
            type42_2.setValue("longField", testLong);
            type42_2.setValue("referenceField", testReference);
            type42_2.setValue("textField", testText);
            type42_2.setValue("timelessDateField", testDate);

            DomainObject type42_3 = client.create("test_type_42");
            type42_3.setValue("stringField", testString);
            type42_3.setValue("booleanField", testBoolean);
            type42_3.setValue("dateTimeField", testDateTime);
            type42_3.setValue("dateTimeWithTimeZoneField", testZonedDateTime);
            type42_3.setValue("decimalField", testDecimal);
            type42_3.setValue("longField", testLong);
            type42_3.setValue("referenceField", testReference);
            type42_3.setValue("textField", testText);
            type42_3.setValue("timelessDateField", testDate);

            List<DomainObject> dataList = new ArrayList<>();
            dataList.add(type42_2);
            dataList.add(type42_3);
            dataList = client.saveAll(dataList);
            assert(dataList.size() == 2);

            // test findLinked
            dataList = client.findLinked(testReference, "test_type_42", "referenceField");
            assert(dataList.size() == 3);

            // test find by query
            String query = "select id, stringField, booleanField, dateTimeField, dateTimeWithTimeZoneField, " +
                    "decimalField, longField, referenceField, " +
                    "textField, timelessDateField " +
                    "from test_type_42 where referenceField = {0}";
            List<CollectionRow> queryResult = client.findByQuery(query,
                    100, 0,
                    client.createFindParam("REFERENCE", testReference));
            assert(queryResult.size() == 3);
            assert(queryResult.get(0).getValue("stringField").equals(testString));
            assert(queryResult.get(0).getValue("booleanField").equals(testBoolean));
            assert(queryResult.get(0).getValue("dateTimeField").equals(testDateTime));
            assert(queryResult.get(0).getValue("dateTimeWithTimeZoneField").equals(testZonedDateTime));
            assert(queryResult.get(0).getValue("decimalField").equals(testDecimal));
            assert(queryResult.get(0).getValue("longField").equals(testLong));
            assert(queryResult.get(0).getValue("referenceField").equals(testReference));
            assert(queryResult.get(0).getValue("textField").equals(testText));
            assert(queryResult.get(0).getValue("timelessDateField").equals(testDate));

            // test setStatus
            assert(type42.getValue("status") == null);
            type42 = client.setStatus(type42.getId(), "Run");
            assert(type42.getValue("status") != null);

            // test create attachment
            File uploadFile = new File("test.rtf");
            DomainObject attachmentInfo = client.createAttachment(type42.getId(), "test_type_42_attch", uploadFile);
            assert(attachmentInfo != null);
            assert(attachmentInfo.getId() != null);

            // test get attachment info
            List<DomainObject> attachmentsInfo = client.getAttachmentsInfo(type42.getId());
            assert(attachmentsInfo != null && attachmentsInfo.size() == 1);
            assert(attachmentsInfo.get(0).getId().equals(attachmentInfo.getId()));

            // test load attachment
            File loadAttachment = client.loadAttachment(attachmentInfo.getId());
            assert(loadAttachment != null);
            assert(loadAttachment.exists());
            long checkSummOriginal = FileUtils.checksumCRC32(uploadFile);
            long checkSummLoad = FileUtils.checksumCRC32(loadAttachment);
            assert(checkSummOriginal == checkSummLoad);

            // test assign task
            WorkflowTaskData taskInfo = new WorkflowTaskData();

            taskInfo.setTaskId("_" + System.currentTimeMillis());
            taskInfo.setProcessId("_" + System.currentTimeMillis());
            taskInfo.setActivityId("_" + System.currentTimeMillis());
            taskInfo.setName("TEST_TASK_FROM_REST");
            taskInfo.setDescription("TEST_TASK_FROM_REST");
            taskInfo.setExecutionId("_" + System.currentTimeMillis());
            taskInfo.setContext(type42.getId());
            taskInfo.addAddresseeItem(new WorkflowTaskAddressee().name("admin").group(false));
            taskInfo.setActions("YES=Да");

            String taskId = client.assignTask(taskInfo);

            DomainObject findTaskInfo =  client.find(taskId);
            assert (findTaskInfo.getValue("name").equals(taskInfo.getName()));

            // test delete attachment
            client.deleteAttachment(attachmentInfo.getId());
            attachmentsInfo = client.getAttachmentsInfo(type42.getId());
            assert(attachmentsInfo != null && attachmentsInfo.size() == 0);

            // test delete
            client.delete(dataList.get(0).getId());
            dataList = client.findLinked(testReference, "test_type_42", "referenceField");
            assert(dataList.size() == 2);

            // test delete all
            List<String> deleteIds = new ArrayList<>();
            deleteIds.add(dataList.get(0).getId());
            deleteIds.add(dataList.get(1).getId());
            client.deleteAll(deleteIds);

            dataList = client.findLinked(testReference, "test_type_42", "referenceField");
            assert(dataList.size() == 0);

            logger.info("Test OK");
        } catch (Throwable ex) {
            logger.error("Error in test", ex);
        }

    }
}
