package ru.intertrust.cm.core.restclient.client;

import ru.intertrust.cm.core.restclient.api.PlatformWsServiceApi;
import ru.intertrust.cm.core.restclient.client.ApiClient;
import ru.intertrust.cm.core.restclient.model.CollectionRowData;
import ru.intertrust.cm.core.restclient.model.DomainObjectData;
import ru.intertrust.cm.core.restclient.model.FindCollectionRequest;
import ru.intertrust.cm.core.restclient.model.ValueData;
import ru.intertrust.cm.core.restclient.model.WorkflowTaskData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Клиент для работы с сервером AF5 через REST API
 */
public class PlatformClient {
    private PlatformWsServiceApi platformWsService;

    /**
     * Создание клиента
     * @param url адрес сервера, указывается до контекста приложения, например http://localhost:8080/cm-sochi
     * @param login логин пользователя
     * @param password пароль пользователя
     */
    public PlatformClient(String url, String login, String password){
        ApiClient client = new ApiClient();
        client.setBasePath(url + "/af5-ws");
        client.setUsername(login);
        client.setPassword(password);
        platformWsService = new PlatformWsServiceApi(client);
    }

    /**
     * Получение доменного объекта по идентификатору
     * @param id идентификатор доменного объекта
     * @return
     */
    public DomainObject find(String id){
        try {
            DomainObjectData data = platformWsService.find(id);
            return DomainObject.get(data);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error find domain object by id " + id, ex);
        }
    }

    /**
     * Создание доменного объекта
     * @param typeName тип создаваемого доменного объекта
     * @return
     */
    public DomainObject create(String typeName){
        try {
            return DomainObject.create(typeName);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error create domain object " + typeName, ex);
        }
    }

    /**
     * Сохранение доменного объекта
     * @param domainObject сохраняемый доменный объектт
     * @return
     */
    public DomainObject save(DomainObject domainObject){
        try {
            DomainObjectData data = platformWsService.save(domainObject.toDomainObjectData());
            return DomainObject.get(data);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error save domain object", ex);
        }
    }

    /**
     * Сохранение нескольких доменных объектов
     * @param domainObjects сохраняемые доменные объекты
     * @return
     */
    public List<DomainObject> saveAll(List<DomainObject> domainObjects){
        try {
            List<DomainObjectData> domainObjectsData = new ArrayList<>();
            for (DomainObject domainObject : domainObjects) {
                domainObjectsData.add(domainObject.toDomainObjectData());
            }

            domainObjectsData = platformWsService.saveAll(domainObjectsData);

            List<DomainObject> result = new ArrayList<>();
            for (DomainObjectData domainObjectData : domainObjectsData) {
                result.add(DomainObject.get(domainObjectData));
            }
            return result;
        }catch(Exception ex){
            throw new PlatformRestClientException("Error save domain objects", ex);
        }
    }

    /**
     * Поиск связанных доменных объектов
     * @param id идентификатор доменного объекта с которым связаны искомые сущности
     * @param linkedType тип связанного доменного объекта
     * @param linkedField поле связанного долменного объекта, которым оно ссылается на исходный доменный объект
     * @return
     */
    public List<DomainObject> findLinked(String id, String linkedType, String linkedField){
        try {
            List<DomainObjectData> domainObjectsData = platformWsService.findLinked(id, linkedType, linkedField);

            List<DomainObject> result = new ArrayList<>();
            for (DomainObjectData domainObjectData : domainObjectsData) {
                result.add(DomainObject.get(domainObjectData));
            }
            return result;
        }catch(Exception ex){
            throw new PlatformRestClientException("Error find linked domain objects", ex);
        }
    }

    /**
     * Удаление доменного объекта
     * @param id идентификатор удаляемлого доменного объекта
     */
    public void delete(String id){
        try {
            platformWsService.delete(id);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error delete domain object", ex);
        }
    }

    /**
     * Удаление нескольких доменных объектов
     * @param ids идентификаторы удаляемых доменных объектов
     */
    public void deleteAll(List<String> ids){
        try {
            platformWsService.deleteAll(ids);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error delete domain objects", ex);
        }
    }

    /**
     * Выполнение поискового запроса
     * @param query запрос
     * @param limit количество строк в результате
     * @param offset смещение результата
     * @param params параметры поиска
     * @return
     */
    public List<CollectionRow> findByQuery(String query, int limit, int offset, ValueData ... params){
        try {

            FindCollectionRequest findRequest = new FindCollectionRequest();
            findRequest.setQuery(query);
            findRequest.setLimit(limit);
            findRequest.setOffset(offset);

            List<ValueData> queryParams = new ArrayList<>();
            for (ValueData param : params) {
                queryParams.add(param);
            }
            findRequest.setParams(queryParams);

            List<CollectionRowData> queryResult = platformWsService.findByQuery(findRequest);

            List<CollectionRow> result = new ArrayList<>();
            for (CollectionRowData collectionRowData : queryResult) {
                result.add(new CollectionRow(collectionRowData));
            }
            return result;
        }catch(Exception ex){
            throw new PlatformRestClientException("Error find by query", ex);
        }
    }

    /**
     * Создание поискового параметра
     * @param type тип параметра
     * @param value значение параметра
     * @return
     */
    public ValueData createFindParam(String type, String value){
        return new ValueData().type(ValueData.TypeEnum.fromValue(type)).value(value);
    }

    /**
     * Создание вложения
     * @param objectId
     * @param attachmentType
     * @param file
     * @return
     */
    public DomainObject createAttachment(String objectId, String attachmentType, File file){
        try {
            DomainObjectData attachmentData = platformWsService.createAttachment(objectId, attachmentType, file);
            return DomainObject.get(attachmentData);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error cretae attachment", ex);
        }
    }

    /**
     * Обновлеиние вложения
     * @param attachmentId идентификатор вложения
     * @param file новое содержание вложения
     * @return
     */
    public DomainObject updateAttachment(String attachmentId, File file){
        try {
            DomainObjectData attachmentData =  platformWsService.updateAttachment(attachmentId, file);
            return DomainObject.get(attachmentData);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error update attachment", ex);
        }
    }

    /**
     * Получение об вложениях, которые привязаны к доменному объекту
     * @param objectId идентификатор доменного объекта, к которому привязаны вложения
     * @return
     */
    public List<DomainObject> getAttachmentsInfo(String objectId){
        try {
            List<DomainObjectData> domainObjectsData = platformWsService.getAttachmentsInfo(objectId);
            List<DomainObject> result = new ArrayList<>();
            for (DomainObjectData domainObjectData : domainObjectsData) {
                result.add(DomainObject.get(domainObjectData));
            }
            return result;
        }catch(Exception ex){
            throw new PlatformRestClientException("Error get attachment info", ex);
        }
    }

    /**
     * Получение вложения
     * @param attachentId идентификатор вложения
     * @return
     */
    public File loadAttachment(String attachentId){
        try {
            File resource = platformWsService.loadAttachment(attachentId);
            return resource;
        }catch(Exception ex){
            throw new PlatformRestClientException("Error load attachment", ex);
        }
    }

    /**
     * Удаление вложения
     * @param attachentId
     */
    public void deleteAttachment(String attachentId){
        try {
            platformWsService.deleteAttachment(attachentId);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error delete attachment", ex);
        }
    }

    /**
     * Установка статуса доменного объекта
     * @param objectId идентификатор доменного объекта
     * @param statusName имя устанавливаемого статуса
     * @return
     */
    public DomainObject setStatus(String objectId, String statusName){
        try {
            DomainObjectData data = platformWsService.setStatus(objectId, statusName);
            return DomainObject.get(data);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error set domain object status", ex);
        }
    }

    /**
     * Создание задачи внешней подсистемой
     * @param taskData
     * @return
     */
    public String assignTask(WorkflowTaskData taskData){
        try{
            return platformWsService.assignTask(taskData);
        }catch(Exception ex){
            throw new PlatformRestClientException("Error assign task", ex);
        }
    }
}
