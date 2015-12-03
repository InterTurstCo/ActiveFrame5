package ru.intertrust.performance.jmetertools;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionItem;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionList;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import com.cedarsoftware.util.io.JsonReader;
import com.google.gwt.user.client.rpc.SerializationException;

public class GwtUtil {
    private static final String RND_STRING_LEGAL_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz _-";
    private static Random rnd = new Random();
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Проверка на то что в ответе ошибка
     * @param responce
     * @return
     */
    public static boolean isError(HTTPSampleResult responce) {
        return responce.getResponseDataAsString().startsWith("//EX");
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
     * @param sampleResult
     * @return
     * @throws SerializationException
     */
    public static Object decodeResponce(HTTPSampleResult sampleResult) throws SerializationException {
        return decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
    }

    /**
     * Декодирование ответа
     * @param request
     * @param responce
     * @param targetUri
     * @return
     * @throws SerializationException
     */
    public static Object decodeResponce(String request, String responce, String targetUri) throws SerializationException {
        try {
            if (request == null || request.length() == 0 || responce == null || responce.length() == 0) {
                return null;
            }

            GwtRpcRequest gwtRpcRequest = GwtRpcRequest.decode(request, targetUri);

            SyncClientSerializationStreamReader reader = new SyncClientSerializationStreamReader(gwtRpcRequest.getSerializationPolicy());
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
    public static WidgetState findWidgetState(GwtRpcRequest request, String widgetId, Class widgetStateClass) throws Throwable {
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
     * Передача измененного запроса в сэмплер
     * @param sampler
     * @param request
     * @throws SerializationException
     */
    public static void setRequest(HTTPSamplerProxy sampler, GwtRpcRequest request) throws SerializationException {
        sampler.getArguments().getArgument(0).setValue(request.encode());
    }

    /**
     * Получение случайной строки в коллекции
     * @param responce
     * @return
     */
    public static CollectionRowItem getRndCollectionsRow(Dto responce) {
        CollectionPluginData collectionPluginData = null;
        if (responce instanceof CollectionPluginData){
            collectionPluginData = (CollectionPluginData) responce;
        }else if(responce instanceof DomainObjectSurferPluginData){
            DomainObjectSurferPluginData data = (DomainObjectSurferPluginData)responce;
            collectionPluginData = data.getCollectionPluginData();
        }
        CollectionRowItem row = null;
        if (collectionPluginData.getItems().size() > 0){
            row = collectionPluginData.getItems().get(rnd.nextInt(collectionPluginData.getItems().size()));
        }
        return row;
    }

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
        CollectionPluginData collectionPluginData = null;
        if (responce instanceof CollectionPluginData){
            collectionPluginData = (CollectionPluginData) responce;
        }else if(responce instanceof DomainObjectSurferPluginData){
            DomainObjectSurferPluginData data = (DomainObjectSurferPluginData)responce;
            collectionPluginData = data.getCollectionPluginData();
        }
        CollectionRowItem row = null;
        for (CollectionRowItem item : collectionPluginData.getItems()) {                    
            if (item.getRowValue(fieldName).equals(value)){
                row = item;
                break;
            }
        }
        return row;
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
    }

    
    /**
     * Создание таблицы соответствия идентификаторов Id в записанном скрипте и в реально принятых данных. Должен вызываться после каждого получения Gwt ответа 
     * @param context
     * @throws Throwable
     */
    public static void updateIdMap(JMeterContext context) throws Throwable {
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
            Object realResponce = decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
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
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();

            GwtRpcRequest request = decodeRequest(sampleProxy);

            IdsMapper mapper = (IdsMapper) context.getVariables().getObject("IdsMapper");
            if (mapper == null) {
                mapper = new IdsMapper();
                context.getVariables().putObject("IdsMapper", mapper);
            }
            mapper.replaceIdsInParams(request.getParameters());

            setRequest(sampleProxy, request);
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
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
            Object realResponce = decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
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
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();

            GwtRpcRequest request = decodeRequest(sampleProxy);

            DomainObjectMapper mapper = (DomainObjectMapper) context.getVariables().getObject("DomainObjectMapper");
            if (mapper == null) {
                mapper = new DomainObjectMapper();
                context.getVariables().putObject("DomainObjectMapper", mapper);
            }
            mapper.replaceIdsInParams(request.getParameters());

            setRequest(sampleProxy, request);
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
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
            GwtRpcRequest request = decodeRequest(sampleProxy);
            try(FileWriter writer = new FileWriter(new File("jmeter-gwt-rpc.log"), true)){
                writer.write("==============================================\n");
                writer.write(sampleProxy.getName() + "\n");
                writer.write(request.asString());
            }
        }
    }
    
    public static void storeUploadResult(JMeterContext context) throws Throwable{
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
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
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();

            GwtRpcRequest request = decodeRequest(sampleProxy);

            UploadMapper mapper = (UploadMapper) context.getVariables().getObject("UploadMapper");
            if (mapper == null) {
                mapper = new UploadMapper();
                context.getVariables().putObject("UploadMapper", mapper);
            }
            
            mapper.replaceUploadResult(request.getParameters());
            setRequest(sampleProxy, request);
        } catch (Throwable ex) {
            log.error("Error replaceUploadResult " + context.getCurrentSampler().getName(), ex);
            throw ex;
        }
    }    
}
