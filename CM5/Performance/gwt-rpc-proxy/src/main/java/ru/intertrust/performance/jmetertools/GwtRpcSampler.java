package ru.intertrust.performance.jmetertools;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import ru.intertrust.performance.gwtrpcproxy.GwtRpcSampleResult;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class GwtRpcSampler extends HTTPSampler {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 692507282661002558L;
    public static final String POLICY_PARAM_PREFIX = "POLICY-";
    
    private Object request;
    private Object response;

    private boolean error;

    @Override
    protected HTTPSampleResult sample(java.net.URL url, String method,
            boolean areFollowingRedirect, int depth) {
        GwtRpcSampleResult result = null;
        try {
            RequestViewer requestViewer = (RequestViewer) getRequest();
            boolean errorOnServer = false;
            Class serviceClass;
            serviceClass = Class.forName(requestViewer.getService());
            Method methodDef = serviceClass.getMethod(requestViewer.getMethod(), requestViewer.getParameterTypes());

            GwtProcySerializationPolicyProvider provider = new GwtProcySerializationPolicyProvider(url.toString());
            String policyAlias = requestViewer.getPolicyNameAlias();
            String policyKey = POLICY_PARAM_PREFIX + policyAlias;
            
            String policyStrongName = getThreadContext().getVariables().get(policyKey);
            SerializationPolicy policy = provider.getSerializationPolicy(requestViewer.getModuleBaseUrl(), policyStrongName);
            GwtRpcRequest request = new GwtRpcRequest(methodDef, requestViewer.getParameters(), policy, 0, requestViewer.getModuleBaseUrl(), policyStrongName, requestViewer.getPolicyNameAlias());

            //getArguments().getArgument(0).setValue(request.encode());
            Arguments arguments = new Arguments();
            HTTPArgument httpArgument = new HTTPArgument("", request.encode(requestViewer.getModuleBaseUrl(), policyStrongName));
            httpArgument.setAlwaysEncoded(false);
            httpArgument.setMetaData("=");
            arguments.addArgument(httpArgument);
            setArguments(arguments);
            HTTPSampleResult origResult = super.sample(url, method, areFollowingRedirect, depth);
            result = new GwtRpcSampleResult();           
            result.init(origResult);
            if (origResult.getResponseDataAsString().startsWith("//EX")){
                errorOnServer = true;
            }
            
            result.setSamplerData("\n\nJSON Data:\n" + request.asString());
            
            SyncClientSerializationStreamReader reader = new SyncClientSerializationStreamReader(policy);
            reader.prepareToRead(result.getResponseDataAsString().substring(4));
            Object responceObj = null;
            Map args = new HashMap();
            args.put(JsonWriter.PRETTY_PRINT, true);
            if (reader.hasData()) {
                responceObj = reader.readObject();
                String json = JsonWriter.objectToJson(responceObj, args);            
                result.setResponseData(json);
                result.setResponseObject(responceObj);
            }else{
                String json = JsonWriter.objectToJson(null, args);   
                result.setResponseData(json);
            }
            
            if (errorOnServer){
                log.error("Error on server: " + this.getName() + " " + result.getResponseDataAsString());
                result.setSuccessful(false);
            }
        } catch (Exception ex) {
            log.error("Error in GwtRpcSampler sampler.", ex);
            ex.printStackTrace();
            result.setResponseData(ex.toString());
            result.setSuccessful(false);            
        }
        return result;
    }

    public Object getRequest() throws UnsupportedEncodingException {
        if (request == null) {
            String requestJson = this.getPropertyAsString("GwtRpcRequestJson");
            requestJson = new String(Base64.decodeBase64(requestJson), "UTF-8");
            request = JsonReader.jsonToJava(requestJson);
        }
        return request;
    }

    public Object getResponse() throws UnsupportedEncodingException {
        if (response == null) {
            String responseJson = this.getPropertyAsString("GwtRpcResponceJson");
            response = new String(Base64.decodeBase64(responseJson), "UTF-8");
        }
        return response;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
    
    @Override
    public HeaderManager getHeaderManager() {
        HeaderManager headerManager = super.getHeaderManager();
        HeaderManager result = null;
        if (headerManager != null){
            result = (HeaderManager)headerManager.clone();
            result.add(new Header("Content-Type", "text/x-gwt-rpc;charset=UTF-8"));
        }
        return result;
    }    

}

