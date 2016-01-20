package ru.intertrust.performance.jmetertools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

public class GwtProcySerializationPolicyProvider implements SerializationPolicyProvider {
    private static final Logger log = LoggerFactory.getLogger(GwtProcySerializationPolicyProvider.class);

    private String lastModuleBaseURL;
    private String lastSerializationPolicyStrongName;
    private String targetUri;
    private static Map<String, SerializationPolicy> polices = new Hashtable<String, SerializationPolicy>();
    private static Map<String, String> policeMap = new Hashtable<String, String>();
    private static int lastPolicyNumber = 0;

    
    public GwtProcySerializationPolicyProvider(String targetUri){
        this.targetUri = targetUri;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
        try {
            URI uri = new URI(moduleBaseURL);
            
            String host = uri.getHost();
            int port = uri.getPort();
            if (targetUri != null){
                URI target = new URI(targetUri);
                
                host = target.getHost();
                port = target.getPort();
            }
            String localModuleBaseURL = new URI(uri.getScheme(), uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(), uri.getFragment()).toString(); 
            lastModuleBaseURL = localModuleBaseURL;
            lastSerializationPolicyStrongName = serializationPolicyStrongName;
            
            SerializationPolicy serializationPolicy = polices.get(serializationPolicyStrongName);
            if (serializationPolicy == null){
                serializationPolicy = SerializationPolicyLoader.loadFromStream(getSerializationPolicyFile(localModuleBaseURL.toString(), serializationPolicyStrongName));
                polices.put(serializationPolicyStrongName, serializationPolicy);
                policeMap.put(String.valueOf(++lastPolicyNumber), serializationPolicyStrongName);
            }
            return serializationPolicy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getSerializationPolicyFile(String moduleBaseURL, String serializationPolicyStrongName) throws ClientProtocolException, IOException, URISyntaxException {
        HttpClient httpclient = new DefaultHttpClient();
        
        HttpGet httpget = new HttpGet(moduleBaseURL.toString() + SerializationPolicyLoader.getSerializationPolicyFileName(serializationPolicyStrongName));
        log.info("Executing request " + httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {

            @Override
            public byte[] handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        byte[] responseBody = httpclient.execute(httpget, responseHandler);
                
        return new ByteArrayInputStream(createPolicyFile(responseBody));
    }

    
    private byte[] createPolicyFile(byte[] responseBody) throws IOException {
        //Подменяем политики сериализации на true
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(responseBody), "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        StringBuilder newPolicyFile = new StringBuilder();
        while (line != null) {
            line = line.trim();
            if (!line.startsWith("@")){
                String[] components = line.split(",");
                newPolicyFile.append(components[0]);
                newPolicyFile.append(",true");
                newPolicyFile.append(",true");
                newPolicyFile.append(",").append(components[3]);
                newPolicyFile.append(",").append(components[4]);
                newPolicyFile.append(",").append(components[5]);
                newPolicyFile.append(",").append(components[6]);
                newPolicyFile.append('\n');
            }
            newPolicyFile.append(line).append('\n');
            line = br.readLine();
        }
        return newPolicyFile.toString().getBytes("UTF-8");
    }

    public String getLastModuleBaseURL() {
        return lastModuleBaseURL;
    }

    public String getLastSerializationPolicyStrongName() {
        return lastSerializationPolicyStrongName;
    }
    
    public static Map<String, String> getPolicyMap(){
        return policeMap;
    }
}
