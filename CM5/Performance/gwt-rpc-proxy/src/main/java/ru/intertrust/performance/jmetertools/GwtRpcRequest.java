package ru.intertrust.performance.jmetertools;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class GwtRpcRequest {
    private String request;
    private RPCRequest rpcRequest;
    private GwtProcySerializationPolicyProvider serializationPolicyProvider;
    private String targetUri;

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
            serializationPolicyProvider = new GwtProcySerializationPolicyProvider(targetUri);
            rpcRequest = RPC.decodeRequest(request, null, serializationPolicyProvider);
        }
    }

    public String encode() throws SerializationException {
        if (rpcRequest != null) {

            SyncClientSerializationStreamWriter writer =
                    new SyncClientSerializationStreamWriter(null, serializationPolicyProvider.getModuleBaseURL(),
                            serializationPolicyProvider.getSerializationPolicyStrongName(), rpcRequest.getSerializationPolicy(), rpcRequest.getRpcToken());
            writer.prepareToWrite();
            writer.writeString(rpcRequest.getMethod().getDeclaringClass().getName());
            writer.writeString(rpcRequest.getMethod().getName());
            writer.writeInt(rpcRequest.getParameters().length);
            for (Object parameter : rpcRequest.getParameters()) {
                writer.writeString(parameter.getClass().getName());
                writer.writeObject(parameter);
            }
            return writer.toString();
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

    public SerializationPolicy getSerializationPolicy() {
        if (serializationPolicyProvider != null) {
            return serializationPolicyProvider.getSerializationPolicy();
        } else {
            return null;
        }
    }

    public String asString() {
        if (rpcRequest != null) {
            RequestViewer requestViewer = new RequestViewer(getServiceClass(), getMethod(), getParameters());
            String json = JsonWriter.objectToJson(requestViewer);
            try {
                json = JsonWriter.formatJson(json);
            } catch (Exception ex) {
            }
            return json;
        } else {
            return null;
        }
    }

}
