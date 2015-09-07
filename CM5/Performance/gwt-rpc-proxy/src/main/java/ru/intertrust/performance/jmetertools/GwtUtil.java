package ru.intertrust.performance.jmetertools;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.client.rpc.SerializationException;

public class GwtUtil {
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz _-";
    private static Random rnd = new Random();
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static boolean isError(HTTPSampleResult responce) {
        return responce.getResponseDataAsString().startsWith("//EX");
    }

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

    public static Object decodeResponce(HTTPSampleResult sampleResult) throws SerializationException {
        return decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
    }

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
     * Возвращает объект состояния виджета
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

    public static WidgetState findWidgetState(GwtRpcRequest request, String widgetId, Class widgetStateClass) throws Throwable {
        try {
            Object parameter = request.getParameters()[0];
            return findWidget(parameter, widgetId, widgetStateClass);
        } catch (Throwable ex) {
            log.error("Error decodeResponce", ex);
            throw ex;
        }
    }

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

    public static String getRndString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void setRequest(HTTPSamplerProxy sampler, GwtRpcRequest request) throws SerializationException {
        sampler.getArguments().getArgument(0).setValue(request.encode());
    }

    public static Id getRndCollectionsRow(Dto responce) {
        CollectionPluginData collectionPluginData = (CollectionPluginData) responce;
        CollectionRowItem item = collectionPluginData.getItems().get(rnd.nextInt(collectionPluginData.getItems().size()));
        return item.getId();
    }

    public static void updateIdMap(JMeterContext context) throws Throwable {
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
            Object realResponce = decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
            String savedResponceJson = sampleProxy.getPropertyAsString("GwtRpcResponceJson");
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

    public static void extractDomainObjects(JMeterContext context) throws Throwable {
        try {
            HTTPSampleResult sampleResult = (HTTPSampleResult) context.getPreviousResult();
            HTTPSamplerProxy sampleProxy = (HTTPSamplerProxy) context.getCurrentSampler();
            Object realResponce = decodeResponce(sampleResult.getQueryString(), sampleResult.getResponseDataAsString(), sampleResult.getURL().toString());
            String savedResponceJson = sampleProxy.getPropertyAsString("GwtRpcResponceJson");
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

}
