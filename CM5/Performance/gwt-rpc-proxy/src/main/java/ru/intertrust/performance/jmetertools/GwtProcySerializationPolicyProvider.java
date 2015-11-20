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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

public class GwtProcySerializationPolicyProvider implements SerializationPolicyProvider {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private String moduleBaseURL;
    private String serializationPolicyStrongName;
    private String targetUri;
    private static Map<String, SerializationPolicy> polices = new Hashtable<String, SerializationPolicy>();

    
    public GwtProcySerializationPolicyProvider(String targetUri){
        this.targetUri = targetUri;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
        try {
            this.moduleBaseURL = moduleBaseURL;
            this.serializationPolicyStrongName = serializationPolicyStrongName;
            String key = moduleBaseURL + "," + serializationPolicyStrongName;
            SerializationPolicy serializationPolicy = polices.get(key);
            if (serializationPolicy == null){
                serializationPolicy = SerializationPolicyLoader.loadFromStream(getSerializationPolicyFile());
                polices.put(key, serializationPolicy);
            }
            return serializationPolicy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getModuleBaseURL() {
        return moduleBaseURL;
    }

    public String getSerializationPolicyStrongName() {
        return serializationPolicyStrongName;
    }

    public SerializationPolicy getSerializationPolicy() {
        return getSerializationPolicy(moduleBaseURL, serializationPolicyStrongName);
    }

    private InputStream getSerializationPolicyFile() throws ClientProtocolException, IOException, URISyntaxException {
        HttpClient httpclient = new DefaultHttpClient();
        
        URI uri = new URI(moduleBaseURL);
        
        String host = uri.getHost();
        int port = uri.getPort();
        if (targetUri != null){
            URI target = new URI(targetUri);
            
            host = target.getHost();
            port = target.getPort();
        }
        URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(), uri.getFragment());
        
        
        HttpGet httpget = new HttpGet(newUri.toString() + SerializationPolicyLoader.getSerializationPolicyFileName(serializationPolicyStrongName));
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
}
