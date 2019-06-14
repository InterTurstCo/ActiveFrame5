package ru.intertrust.cm.core.business.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.ClusterNodeInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckData;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckLockData;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckResult;
import ru.intertrust.cm.core.business.api.dto.globalcache.CheckResultItem;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.globalcacheclient.cluster.GlobalCacheJmsHelper;

@RestController
public class CheckClobalCacheInvalidation {
    final static Logger logger = LoggerFactory.getLogger(CheckClobalCacheInvalidation.class);

    public static final String TEST_RESOURCE = "testLock";

    @Autowired
    private ClusterManager cluserManager;

    @Autowired
    private GlobalCacheJmsHelper jmsHelper;

    @Autowired
    private CrudService crudService;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private InterserverLockingService lockingService;

    /**
     * Проверка инвалидации кэша для доменных объектов, измененных на других
     * узлах. Инициатор создает запись string_resources для каждого узла
     * кластера и отправляет уведомления об этом. Узлы принимают это сообщение,
     * меняют каждый свой string_resources Инициатор ожидает таймаут и
     * зачитывает все string_resources. Они должны быть все измененные. В
     * качестве метки изменения используется дата модификации
     * @param timeout
     * @return
     */
    @RequestMapping(value = "/globalcache/check/{timeout}", method = RequestMethod.GET)
    public CheckResult check(@PathVariable(value = "timeout") Integer timeout) {
        try {
            Map<String, ClusterNodeInfo> nodesInfo = cluserManager.getNodesInfo();
            List<DomainObject> testResources = new ArrayList<DomainObject>();

            CheckData checkData = new CheckData();

            for (String nodeId : nodesInfo.keySet()) {
                IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                        "select id from resources where name = {0}", Collections.singletonList(new StringValue(nodeId)));
                DomainObject testResource = null;
                if (collection.size() == 0) {
                    testResource = crudService.createDomainObject("string_resources");
                } else {
                    testResource = crudService.find(collection.get(0).getId());
                }
                testResource.setString("name", nodeId);
                testResource.setString("string_value", nodeId);
                testResource = crudService.save(testResource);

                // Зачитываем данные чтоб они осели в кэше
                testResource = crudService.find(testResource.getId());

                // Запоминаем состояние
                testResources.add(testResource);
            }

            // Формируем проверочное сообщение
            CacheInvalidation checkMessage = new CacheInvalidation();
            checkMessage.setDiagnosticData(checkData);

            //Отправляем 
            jmsHelper.sendClusterNotification(checkMessage);

            // Спим таймаут
            Thread.currentThread().sleep(timeout);

            CheckResult result = new CheckResult();
            result.setInitiator(cluserManager.getNodeId());
            // Проверяем изменились ли данные в кэше
            for (DomainObject savedTestResource : testResources) {
                DomainObject testResource = crudService.find(savedTestResource.getId());
                result.getCheckData().put(savedTestResource.getString("name"),
                        new CheckResultItem(nodesInfo.get(testResource.getString("name")).getNodeName(),
                                testResource.getModifiedDate().after(savedTestResource.getModifiedDate())));

                // Удаляем мусор
                crudService.delete(testResource.getId());
            }

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error execute check command", ex);
        }

    }

    /**
     * Проверка работоспособности распределенной блокировки Создается тестовый
     * ресурс, сохраняется. Далее создается блокировка ресурса "checkLock".
     * Отправляется служебное сообщение всем, что начинается процесс проверки, и
     * высылается идентификатор созданного ресурса. Все узлы зачитывают этот
     * ресурс, он оседает в локальных кэшах всех узлов. Все узлы пытаются
     * заблокировать этот же ресурс "checkLock" и ожидают. Основной сервер
     * ожидает 1 сек и далее обновляет тестовый ресурс в транзакции, записывает
     * туда свой ID и снимает блокировку. Первый случайный из узлов получает
     * блокировку, и в свою очередь блокирует ресурс, записывает туда свой ID в
     * дополнение тому что там уже есть, и освобождает ресурс. Так делают все
     * узлы кластера. Инициатор проверки ждет таймаут заданный в URL, получает
     * все узлы кластера и проверяет есть ли изменения от конкретных узлов.
     * Результат выводит в виде json
     * 
     * @param timeout
     * @return
     */
    @RequestMapping(value = "/globalcache/checklock/{timeout}", method = RequestMethod.GET)
    public CheckResult checkLock(@PathVariable(value = "timeout") Integer timeout) {
        try {

            // Находим или создаем ресурс
            IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(
                    "select id from resources where name = {0}", Collections.singletonList(new StringValue(TEST_RESOURCE)));
            DomainObject testResource = null;
            if (collection.size() == 0) {
                testResource = crudService.createDomainObject("string_resources");
            } else {
                testResource = crudService.find(collection.get(0).getId());
            }
            testResource.setString("name", TEST_RESOURCE);
            testResource.setString("string_value", "");
            testResource = crudService.save(testResource);

            // Создаем блокировку
            lockingService.lock(TEST_RESOURCE);

            // Отправляем сообщение с ID ресурса
            CheckLockData checkData = new CheckLockData();
            checkData.setLockResourceId(testResource.getId());

            CacheInvalidation checkMessage = new CacheInvalidation();
            checkMessage.setDiagnosticData(checkData);
            
            //Отправляем 
            jmsHelper.sendClusterNotification(checkMessage);

            // Спим секунду
            Thread.currentThread().sleep(1000);

            // Изменеяем ресурс
            testResource.setString("string_value", cluserManager.getNodeId());
            crudService.save(testResource);

            // Снимаем блокировку
            lockingService.unlock(TEST_RESOURCE);

            // Спим таймаут
            Thread.currentThread().sleep(timeout);
            
            // В свою очередь ждем освобождение блокировок 
            lockingService.waitUntilNotLocked(CheckClobalCacheInvalidation.TEST_RESOURCE);
            
            // Формируем результат
            CheckResult result = new CheckResult();
            result.setInitiator(cluserManager.getNodeId());

            // Перезачитываем, так как объект меняли все узлы
            testResource = crudService.find(testResource.getId());
            String value = testResource.getString("string_value");
            Map<String, ClusterNodeInfo> nodesInfo = cluserManager.getNodesInfo();
            for (String nodeId : nodesInfo.keySet()) {
                // Добавляем в результат узлы и результат того были ли изменения сделаны на этом узле
                result.getCheckData().put(nodeId,
                        new CheckResultItem(nodesInfo.get(nodeId).getNodeName(), value.contains(nodeId)));
            }

            // Удаляем мусор
            crudService.delete(testResource.getId());

            return result;
        } catch (Exception ex) {
            throw new FatalException("Error execute check lock command", ex);
        }
    }

}
