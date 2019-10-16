package ru.intertrust.performance.jmetertools;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.client.rpc.SerializationException;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionItem;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionList;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.performance.gwtrpcproxy.GwtRpcSampleResult;

public class GwtUtil {
    private static final String RND_STRING_LEGAL_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz _-";
    private static Random rnd = new Random();
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Проверка на то что в ответе ошибка
     * @param responce
     * @return
     */
    public static boolean isError(JMeterContext context) {
        boolean result = false;
        if (context.getCurrentSampler() instanceof GwtRpcSampler){
            GwtRpcSampler sampler = (GwtRpcSampler)context.getCurrentSampler();
            if (sampler.isError()){
                result = true;         
            }
        }
        if(!context.getPreviousResult().isSuccessful()){
            result = true;
        }
        return result;
    }

    /**
     * Декодирование запроса
     * @param sampleProxy
     * @return
     * @throws MalformedURLException
     */
    public static GwtRpcRequest decodeRequest(HTTPSamplerProxy sampleProxy) throws MalformedURLException {
        try {
            return GwtRpcRequest.decode(
                    ((org.apache.jmeter.protocol.http.util.HTTPArgument) sampleProxy.getArguments().getArgument(0)).getValue(),
                    sampleProxy.getUrl().toString());
        } catch (Throwable ex) {
            log.error("Error decodeRequest " + sampleProxy.getName(), ex);
            throw ex;
        }
    }

    /**
     * Декодирование ответа
     * @param request
     * @param responce
     * @param targetUri
     * @return
     * @throws SerializationException
     */
    public static Object decodeResponce(String moduleBaseUrl, String policyStrongName, String responce, String targetUri) throws SerializationException {
        try {
            if (responce == null || responce.length() == 0) {
                return null;
            }

            GwtProcySerializationPolicyProvider provider = new GwtProcySerializationPolicyProvider(targetUri);

            
            
            SyncClientSerializationStreamReader reader = new SyncClientSerializationStreamReader(provider.getSerializationPolicy(moduleBaseUrl, policyStrongName));
            reader.prepareToRead(responce.substring(4));
            Object responceObj = null;
            if (reader.hasData()) {
                responceObj = reader.readObject();
            }
            return responceObj;
        } catch (Throwable ex) {
            log.error("Error decodeResponce", ex);
            throw ex;
        }

    }

    /**
     * Возвращает объект состояния виджета. Виджет ищется только в верхней по иерархии форме
     * @param request
     * @param widgetId
     * @return
     */
    public static WidgetState getWidgetState(GwtRpcRequest request, String widgetId) {
        //Получаем нужный виджет
        Command command = (Command) request.getParameters()[0];
        SaveActionContext saveContext = (SaveActionContext) command.getParameter();
        FormState formState = saveContext.getFormState();
        Map widgetStateMap = formState.getFullWidgetsState();
        WidgetState widgetState = (WidgetState) widgetStateMap.get(widgetId);
        return widgetState;
    }

    /**
     * Получение состояния виджета. Виджет ищется во всех вложенных формах
     * @param request
     * @param widgetId
     * @param widgetStateClass
     * @return
     * @throws Throwable
     */
    public static WidgetState findWidgetState(RequestViewer request, String widgetId, Class widgetStateClass) throws Throwable {
        try {
            Object parameter = request.getParameters()[0];
            return findWidget(parameter, widgetId, widgetStateClass);
        } catch (Throwable ex) {
            log.error("Error decodeResponce", ex);
            throw ex;
        }
    }

    /**
     * Рекурсивный поиск состояния виджета по имени и классу
     * @param request
     * @param widgetId
     * @param widgetStateClass
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static WidgetState findWidget(Object request, String widgetId, Class widgetStateClass) throws IllegalArgumentException, IllegalAccessException {
        Class superClass = request.getClass();
        do {
            Field[] fields = superClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                if (Map.class.isAssignableFrom(field.getType())) {
                    Map savedMap = (Map) field.get(request);
                    if (savedMap != null) {
                        for (Object key : savedMap.keySet()) {
                            Object value = savedMap.get(key);
                            if (value != null && key.equals(widgetId) && widgetStateClass.isAssignableFrom(value.getClass())) {
                                return (WidgetState) value;
                            }
                        }
                    }
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection values = (Collection) field.get(request);
                    for (Object value : values) {
                        return findWidget(value, widgetId, widgetStateClass);
                    }
                } else if (Dto.class.isAssignableFrom(field.getType()) && !field.getType().isEnum()) {
                    Dto savedObj = (Dto) field.get(request);
                    if (savedObj != null) {
                        return findWidget(savedObj, widgetId, widgetStateClass);
                    }
                }
            }
            superClass = superClass.getSuperclass();
        } while (!superClass.equals(Object.class));
        return null;
    }

    /**
     * Формирование случайной строки заданной длины
     * @param length количество символов в случайной строке
     * @return
     */
    public static String getRndString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(RND_STRING_LEGAL_CHARS.charAt(rnd.nextInt(RND_STRING_LEGAL_CHARS.length())));
        return sb.toString();
    }


    /**
     * Получение случайной строки в коллекции
     * @param responce
     * @return
     */
    public static CollectionRowItem getRndCollectionsRow(Dto responce) {
        ArrayList<CollectionRowItem> items = getCollectionRowItems(responce);
        CollectionRowItem row = null;
        if (items != null && items.size() > 0){
            row = items.get(rnd.nextInt(items.size()));
        }
        return row;
    }

    /**
     * Получение случайного значения в выпадающем списке SuggestionList 
     * @param responce
     * @return
     */
    public static SuggestionItem getRndSuggestionItem(Dto responce) {
        SuggestionList suggestionList = null;
        if (responce instanceof SuggestionList){
            suggestionList = (SuggestionList) responce;
        }
        SuggestionItem suggestionItem = null;
        if (suggestionList != null && suggestionList.getSuggestions().size() > 0){
            suggestionItem = suggestionList.getSuggestions().get(rnd.nextInt(suggestionList.getSuggestions().size()));
        }
        return suggestionItem;
    }
    
    
    /**
     * Поиск строки в коллекции по условию
     * @param responce
     * @param fieldName
     * @param value
     * @return
     */
    public static CollectionRowItem findCollectionsRow(Dto responce, String fieldName, Value value) {
        ArrayList<CollectionRowItem> items = getCollectionRowItems(responce);
        CollectionRowItem row = null;
        for (CollectionRowItem item : items) {                    
            if (item.getRowValue(fieldName).equals(value)){
                row = item;
                break;
            }
        }
        return row;
    }
    
    /**
     * Получение коллекции строк из разных структур ее содержащих
     * @param responce
     * @return
     */
    public static ArrayList<CollectionRowItem> getCollectionRowItems(Dto responce){
        ArrayList<CollectionRowItem> items = null;
        if (responce instanceof CollectionPluginData){
            CollectionPluginData collectionPluginData = (CollectionPluginData) responce;
            items = collectionPluginData.getItems();
        }else if(responce instanceof DomainObjectSurferPluginData){
            DomainObjectSurferPluginData data = (DomainObjectSurferPluginData)responce;
            CollectionPluginData collectionPluginData = data.getCollectionPluginData();
            items = collectionPluginData.getItems();
        }else if(responce instanceof CollectionRowsResponse){
            CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse)responce;
            items = collectionRowsResponse.getCollectionRows();
        }
        return items;
    }
    
    /**
     * Очистка соответствия идентификаторов и доменных объектов
     */
    public static void clearDomainObjectMaps(JMeterContext context){
        IdsMapper mapper = (IdsMapper) context.getVariables().getObject("IdsMapper");
        if (mapper != null) {
            mapper.clear();
        }
        DomainObjectMapper doMapper = (DomainObjectMapper) context.getVariables().getObject("DomainObjectMapper");
        if (doMapper != null) {
            doMapper.clear();
        }
        UploadMapper uploadMapper = (UploadMapper)  context.getVariables().getObject("UploadMapper");
        if (uploadMapper != null) {
            uploadMapper.clear();
        }        
    }

    
    /**
     * Создание таблицы соответствия идентификаторов Id в записанном скрипте и в реально принятых данных. Должен вызываться после каждого получения Gwt ответа 
     * @param context
     * @throws Throwable
     */
    public static void updateIdMap(JMeterContext context) throws Throwable {
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();
            Object realResponce = JsonReader.jsonToJava(sampleResult.getResponseDataAsString());
            String savedResponceJson = new String(Base64.decodeBase64(sampleProxy.getPropertyAsString("GwtRpcResponceJson")), "UTF-8");
            Object savedResponce = JsonReader.jsonToJava(savedResponceJson);

            IdsMapper mapper = (IdsMapper) context.getVariables().getObject("IdsMapper");
            if (mapper == null) {
                mapper = new IdsMapper();
                context.getVariables().putObject("IdsMapper", mapper);
            }
            mapper.addToMap(savedResponce, realResponce);

        } catch (Throwable ex) {
            log.error("Error updateIdMap " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }

    /**
     * Обновление сохраненного запроса путем замены в нем идентификаторов, сохраненных методом updateIdMap. Должен вызываться перед отправкой запроса на сервер GWT 
     * @param context
     * @throws Throwable
     */
    public static void applyIdMap(JMeterContext context) throws Throwable {
        try {
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();

            //GwtRpcRequest request = decodeRequest(sampleProxy);
            RequestViewer request = (RequestViewer)sampleProxy.getRequest();

            IdsMapper mapper = (IdsMapper) context.getVariables().getObject("IdsMapper");
            if (mapper == null) {
                mapper = new IdsMapper();
                context.getVariables().putObject("IdsMapper", mapper);
            }
            mapper.replaceIdsInParams(request.getParameters());

            //setRequest(sampleProxy, request);
        } catch (Throwable ex) {
            log.error("Error applyIdMap " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }

    /**
     * Извлечение доменных объектов из ответа сервера и сохранение их в таблице соответствия доменных объектов. Должен вызываться после получения ответа от сервера 
     * @param context
     * @throws Throwable
     */
    public static void extractDomainObjects(JMeterContext context) throws Throwable {
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();
            Object realResponce = JsonReader.jsonToJava(sampleResult.getResponseDataAsString());
            String savedResponceJson = new String(Base64.decodeBase64(sampleProxy.getPropertyAsString("GwtRpcResponceJson")), "UTF-8");

            Object savedResponce = JsonReader.jsonToJava(savedResponceJson);

            DomainObjectMapper mapper = (DomainObjectMapper) context.getVariables().getObject("DomainObjectMapper");
            if (mapper == null) {
                mapper = new DomainObjectMapper();
                context.getVariables().putObject("DomainObjectMapper", mapper);
            }
            mapper.addToMap(savedResponce, realResponce);

        } catch (Throwable ex) {
            log.error("Error updateIdMap " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }

    /**
     * Замена доменных объектов в сохраненном запросе на те доменные объекты которые были ранее получены в ответах, и сохранены с помощью extractDomainObjects
     * @param context
     * @throws Throwable
     */
    public static void replaceDomainObjects(JMeterContext context) throws Throwable {
        try {
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();

            //GwtRpcRequest request = decodeRequest(sampleProxy);
            RequestViewer request = (RequestViewer)sampleProxy.getRequest();

            DomainObjectMapper mapper = (DomainObjectMapper) context.getVariables().getObject("DomainObjectMapper");
            if (mapper == null) {
                mapper = new DomainObjectMapper();
                context.getVariables().putObject("DomainObjectMapper", mapper);
            }
            mapper.replaceIdsInParams(request.getParameters());

            //setRequest(sampleProxy, request);
        } catch (Throwable ex) {
            log.error("Error applyIdMap " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }
    
    /**
     * Метод обработки ответа сервера. Сохраняет в контексте информацию о полученных доменных объектах и идентификаторах
     * @param context
     * @throws Throwable
     */
    public static void postResponseProcessing(JMeterContext context)throws Throwable{
        extractDomainObjects(context);
        updateIdMap(context);      
        boolean store = Boolean.parseBoolean(context.getVariables().get("STORE_REAL_REQUEST"));
        if (store){
            Object response = ((GwtRpcSampleResult)context.getPreviousResult()).getResponseObject();
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();
            Map args = new HashMap();
            args.put(JsonWriter.PRETTY_PRINT, true);
            String json = JsonWriter.objectToJson(response, args);
            try(Writer writer = new OutputStreamWriter(new FileOutputStream("jmeter-gwt-rpc.log", true), "UTF-8")){
                writer.write("======================Response========================\n");
                writer.write(sampleProxy.getName() + "\n");
                writer.write(json);
                writer.write("\n");
            }
        }        
    }
    
    /**
     * Метод подготовки ответа, перед отправкой его на сервер. Производит замену в сохраненном ответе доменных объектов и идентификаторов, сохраненных ранее при анализе ответов от сервера. 
     * @param context
     * @throws Throwable
     */
    public static void preRequestProcessing(JMeterContext context)throws Throwable{
        replaceDomainObjects(context);
        applyIdMap(context);
        replaceUploadResult(context);
        
        boolean store = Boolean.parseBoolean(context.getVariables().get("STORE_REAL_REQUEST"));
        if (store){
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();
            Object request = sampleProxy.getRequest();
            Map args = new HashMap();
            args.put(JsonWriter.PRETTY_PRINT, true);  
            String json = JsonWriter.objectToJson(request, args);
            try(Writer writer = new OutputStreamWriter(new FileOutputStream("jmeter-gwt-rpc.log", true), "UTF-8")){
                writer.write("======================Request========================\n");
                writer.write(sampleProxy.getName() + "\n");
                writer.write(json);
                writer.write("\n");
            }
        }
    }
    
    public static void storeUploadResult(JMeterContext context) throws Throwable{
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            HTTPSamplerBase sampleProxy = (HTTPSamplerBase) context.getCurrentSampler();
            String realResponce = sampleResult.getResponseDataAsString();
            String savedResponce = sampleProxy.getPropertyAsString("GwtRpcResponceJson");

            UploadMapper mapper = (UploadMapper) context.getVariables().getObject("UploadMapper");
            if (mapper == null) {
                mapper = new UploadMapper();
                context.getVariables().putObject("UploadMapper", mapper);
            }
            mapper.addToMap(savedResponce, realResponce);

        } catch (Throwable ex) {
            log.error("Error storeUploadResult " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }        
    }
    
    public static void replaceUploadResult(JMeterContext context) throws Throwable {
        try {
            GwtRpcSampler sampleProxy = (GwtRpcSampler) context.getCurrentSampler();

            //GwtRpcRequest request = decodeRequest(sampleProxy);
            RequestViewer request = (RequestViewer)sampleProxy.getRequest();

            UploadMapper mapper = (UploadMapper) context.getVariables().getObject("UploadMapper");
            if (mapper == null) {
                mapper = new UploadMapper();
                context.getVariables().putObject("UploadMapper", mapper);
            }
            
            mapper.replaceUploadResult(request.getParameters());
            //setRequest(sampleProxy, request);
        } catch (Throwable ex) {
            log.error("Error replaceUploadResult " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }
    
    public static Id getSavedId(SaveActionData saveActionData){
        Id result = saveActionData.getFormPluginData().getFormDisplayData().getFormState().getObjects().getRootDomainObject().getId();
        return result;
    }
}
