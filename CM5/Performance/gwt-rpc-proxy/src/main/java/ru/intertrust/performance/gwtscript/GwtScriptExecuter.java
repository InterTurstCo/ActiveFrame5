package ru.intertrust.performance.gwtscript;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.filter.Filter;
import org.simpleframework.xml.filter.MapFilter;
import org.simpleframework.xml.strategy.Strategy;

import ru.intertrust.performance.gwtrpcproxy.GwtInteraction;
import ru.intertrust.performance.gwtrpcproxy.GwtInteractionGroup;
import ru.intertrust.performance.gwtrpcproxy.GwtRpcJournal;

public class GwtScriptExecuter {
    private String uri;

    public GwtScriptExecuter(String uri) {
        this.uri = uri;
    }

    public static void main(String[] args) {
        try {

            if (args.length < 2) {
                System.out.println("Usage: ru.intertrust.performance.gwtscript.GwtScriptExecuter <targetUri> <scfript_file>");
                System.out.println("Example: ru.intertrust.performance.gwtscript.GwtScriptExecuter http://cm45.inttrust.ru:8080 8090 my_gwtProxyScript.xml");
                System.exit(1);
            }

            String uri = args[0];
            String scriptFile = args[1];

            GwtScriptExecuter gwtScriptExecuter = new GwtScriptExecuter(uri);
            gwtScriptExecuter.start(gwtScriptExecuter.readFile(scriptFile), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void start(String readFile, ScriptExecutionContext scriptContext) throws Exception {

        URI targetUri = new URI(uri);

        Map map = new HashMap();
        Filter filter = new MapFilter(map);
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy, filter);
        ByteArrayInputStream buffer = new ByteArrayInputStream(readFile.getBytes("UTF8"));
        GwtRpcJournal journal = serializer.read(GwtRpcJournal.class, buffer);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        ScriptExecutionContext localScriptContext = scriptContext;
        if (localScriptContext == null) {
            localScriptContext = new ScriptExecutionContext();
        }

        for (GwtInteractionGroup group : journal.getGroupList()) {
            for (GwtInteraction pair : group.getRequestResponceList()) {
                HttpUriRequest request = null;
                if (pair.getRequest().getMethod().equalsIgnoreCase("get")) {
                    request = new HttpGet(targetUri.toString() + pair.getRequest().getUrl());
                } else if (pair.getRequest().getMethod().equalsIgnoreCase("post")) {
                    request = new HttpPost(targetUri.toString() + pair.getRequest().getUrl());

                    String body = pair.getRequest().getBody();

                    StringEntity entity = new StringEntity(body);
                    entity.setContentType(pair.getRequest().getContentType());
                    ((HttpPost) request).setEntity(entity);
                    // TODO Наверно надо хранить в скрипте
                    request.addHeader("X-GWT-Permutation", "A3EF20039C3580B544F259C1A09C834B");
                    System.out.println("Request body: " + EntityUtils.toString(entity));
                }

                System.out.println("Request: " + request.getRequestLine());

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }

                };
                String responseBody = httpclient.execute(request, responseHandler);

                System.out.println("Responce: " + responseBody);
                System.out.println("----------------------------------------");
            }
        }

    }

    /*public void start(String readFile) throws Exception {
        
        URI targetUri = new URI(uri);
        
        Serializer serializer = new Persister();
        ByteArrayInputStream buffer = new ByteArrayInputStream(readFile.getBytes("UTF8"));
        GwtRpcJournal journal = serializer.read(GwtRpcJournal.class, buffer);

        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("Test/1.1"))
                .add(new RequestExpectContinue(true)).build();        
        
        HttpCoreContext coreContext = HttpCoreContext.create();
        HttpHost host = new HttpHost(targetUri.getHost(), targetUri.getPort());
        coreContext.setTargetHost(host);

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024);
        ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

        
        for (GwtRequestResponce pair : journal.getRequestResponceList()) {
            
            if (!conn.isOpen()) {
                Socket socket = new Socket(host.getHostName(), host.getPort());
                conn.bind(socket);
            }
            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(pair.getRequest().getMethod(), pair.getRequest().getUrl());
            StringEntity entity = new StringEntity(pair.getRequest().getBody());
            entity.setContentType(pair.getRequest().getContentType());
            request.setEntity(entity);
            request.addHeader("X-GWT-Permutation", "A3EF20039C3580B544F259C1A09C834B");
            
            System.out.println(">> Request URI: " + request.getRequestLine().getUri());

            httpexecutor.preProcess(request, httpproc, coreContext);
            HttpResponse response = httpexecutor.execute(request, conn, coreContext);
            httpexecutor.postProcess(response, httpproc, coreContext);

            System.out.println("<< Response: " + response.getStatusLine());
            System.out.println(EntityUtils.toString(response.getEntity()));
            System.out.println("==============");
            if (!connStrategy.keepAlive(response, coreContext)) {
                conn.close();
            } else {
                System.out.println("Connection kept alive...");
            }            
        }
    }*/

    private String readFile(String file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), "UTF8");
        } finally {
            input.close();
        }
    }
}
