package ru.intertrust.performance.jmetertools;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class GwtRpcRequest {
    private String request;
    private RPCRequest rpcRequest;
    private String targetUri;
    private String moduleBaseUrl;
    private String policyStrongName;
    private String policyAlias;
    

    public GwtRpcRequest(Method method, Object[] parameters, SerializationPolicy serializationPolicy, int flags, String moduleBaseUrl, String policyStrongName, String policyAlias) {
        rpcRequest = new RPCRequest(method, parameters, serializationPolicy, flags);
        this.moduleBaseUrl = moduleBaseUrl;
        this.policyStrongName = policyStrongName;
        this.policyAlias = policyAlias;
    }

    private GwtRpcRequest(String request) {
        this.request = request;
    }

    private GwtRpcRequest(String request, String targetUri) {
        this.request = request;
        this.targetUri = targetUri;
    }

    public static GwtRpcRequest decode(String request) {
        GwtRpcRequest result = new GwtRpcRequest(request);
        result.decode();
        return result;
    }

    public static GwtRpcRequest decode(String request, String targetUri) {
        GwtRpcRequest result = new GwtRpcRequest(request, targetUri);
        result.decode();
        return result;
    }

    private void decode() {
        if (request != null && request.length() > 0) {
            GwtProcySerializationPolicyProvider serializationPolicyProvider = new GwtProcySerializationPolicyProvider(targetUri);
            rpcRequest = RPC.decodeRequest(request, null, serializationPolicyProvider);
            policyStrongName = serializationPolicyProvider.getLastSerializationPolicyStrongName();
            moduleBaseUrl = serializationPolicyProvider.getLastModuleBaseURL();
            for (String alias : serializationPolicyProvider.getPolicyMap().keySet()) {
                if (serializationPolicyProvider.getPolicyMap().get(alias).equals(policyStrongName)){
                    policyAlias = alias;
                    break;
                }
            }             
        }
    }

    public String encode(String moduleBaseURL, String policyStrongName) throws SerializationException {
        if (rpcRequest != null) {

            SyncClientSerializationStreamWriter writer =
                    new SyncClientSerializationStreamWriter(null, moduleBaseURL,
                            policyStrongName, rpcRequest.getSerializationPolicy(), rpcRequest.getRpcToken());
            writer.prepareToWrite();
            writer.writeString(rpcRequest.getMethod().getDeclaringClass().getName());
            writer.writeString(rpcRequest.getMethod().getName());
            writer.writeInt(rpcRequest.getParameters().length);
            for (int i = 0; i < rpcRequest.getMethod().getParameterTypes().length; i++) {
                writer.writeString(rpcRequest.getMethod().getParameterTypes()[i].getName());
                writer.writeObject(rpcRequest.getParameters()[i]);
            }
            String result = writer.toString();
            return result;
        } else {
            return null;
        }

    }

    public Object[] getParameters() {
        if (rpcRequest != null) {
            return rpcRequest.getParameters();
        } else {
            return null;
        }
    }

    public String getMethod() {
        if (rpcRequest != null) {
            return rpcRequest.getMethod().getName();
        } else {
            return null;
        }
    }

    public String getServiceClass() {
        if (rpcRequest != null) {
            return rpcRequest.getMethod().getDeclaringClass().getName();
        } else {
            return null;
        }
    }

    public String getModuleBaseUrl() {
        return moduleBaseUrl;
    }

    public String getPolicyStrongName(){
        return policyStrongName;
    }
    
    public Class[] getParameterTypes() {
        if (rpcRequest != null) {
            return rpcRequest.getMethod().getParameterTypes();
        } else {
            return null;
        }
    }

    public String asString() {
        if (rpcRequest != null) {
            
            RequestViewer requestViewer = new RequestViewer(
                    getServiceClass(), getMethod(), getParameters(), moduleBaseUrl, getParameterTypes(), policyAlias);
            Map args = new HashMap();
            args.put(JsonWriter.PRETTY_PRINT, true);
            String json = JsonWriter.objectToJson(requestViewer, args);
            return json;
        } else {
            return null;
        }
    }

}
