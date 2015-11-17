package ru.intertrust.performance.gwtrpcproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import ru.intertrust.performance.jmetertools.GwtRpcRequest;
import ru.intertrust.performance.jmetertools.GwtUtil;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.gwt.user.client.rpc.SerializationException;

public class ProxyContext {
    private int localPort;
    private String targetUri;
    private String outFile = "gwtProxyOut.xml";
    private final GwtRpcJournal journal = new GwtRpcJournal();
    private int groupPause;
    private long lastIteractionTime = 0;
    private int groupCount = 0;
    private GwtInteractionGroup group;

    public ProxyContext() {
        journal.setGroupList(new ArrayList<GwtInteractionGroup>());
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public void addResponce(ProxyHttpExchange proxyHttpExchange) {
        try {
            synchronized (journal) {

                GwtInteraction requestResponce = new GwtInteraction();
                GwtRequest request = new GwtRequest();
                request.setMethod(proxyHttpExchange.getRequest().getRequestLine().getMethod());
                request.setUrl(proxyHttpExchange.getRequest().getRequestLine().getUri());
                if (proxyHttpExchange.getRequest() instanceof BasicHttpEntityEnclosingRequest) {
                    BasicHttpEntityEnclosingRequest basicHttpEntityEnclosingRequest = (BasicHttpEntityEnclosingRequest) proxyHttpExchange.getRequest();
                    request.setContentType(((BasicHttpEntityEnclosingRequest) proxyHttpExchange.getRequest()).getEntity().getContentType().getValue());
                    if (request.getContentType().contains("multipart/form-data")) {
                        FileInfo fileInfo = saveFile(proxyHttpExchange.getInStream().toByteArray(),
                                basicHttpEntityEnclosingRequest.getEntity().getContentLength(),
                                basicHttpEntityEnclosingRequest.getEntity().getContentEncoding() != null ? basicHttpEntityEnclosingRequest.getEntity()
                                        .getContentEncoding().getValue() : null, request.getContentType());
                        request.setFile(fileInfo);
                    } else {
                        request.setBody(
                                getBody(proxyHttpExchange.getInStream().toByteArray(),
                                        basicHttpEntityEnclosingRequest.getEntity().getContentLength(),
                                        basicHttpEntityEnclosingRequest.getEntity().getContentEncoding() != null ? basicHttpEntityEnclosingRequest.getEntity()
                                                .getContentEncoding().getValue() : null));
                        GwtRpcRequest gwtRpcRequest = GwtRpcRequest.decode(request.getBody(), targetUri);
                        request.setJson(gwtRpcRequest.asString());
                    }
                }
                GwtResponce responce = new GwtResponce();

                responce.setStatus(proxyHttpExchange.getResponse().getStatusLine().getStatusCode());
                if (proxyHttpExchange.getResponse().getEntity() != null) {
                    responce.setBody(getBody(proxyHttpExchange.getOutStream().toByteArray(),
                            proxyHttpExchange.getResponse().getEntity().getContentLength(),
                            proxyHttpExchange.getResponse().getEntity().getContentEncoding() != null ? proxyHttpExchange.getResponse().getEntity()
                                    .getContentEncoding().getValue() : null
                            ));
                    responce.setContentType(proxyHttpExchange.getResponse().getEntity().getContentType() != null ? proxyHttpExchange.getResponse().getEntity()
                            .getContentType().getValue() : null);
                    responce.setJson(decodeResponceToJson(request.getBody(), responce.getBody(), targetUri));
                }

                requestResponce.setRequest(request);
                requestResponce.setResponce(responce);

                //Сохраняем только нужные вызовы
                if (isSaveRequest(requestResponce)) {
                    //Определяем надо ли создавать новую группу
                    if ((System.currentTimeMillis() - lastIteractionTime) > (groupPause * 1000)) {
                        group = new GwtInteractionGroup();
                        group.setRequestResponceList(new ArrayList<GwtInteraction>());
                        group.setName("Group " + groupCount);
                        groupCount++;
                        journal.getGroupList().add(group);
                        lastIteractionTime = System.currentTimeMillis();
                    }

                    group.getRequestResponceList().add(requestResponce);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String decodeResponceToJson(String request, String responce, String targetUri) throws SerializationException {
        if (request == null || request.length() == 0 || responce == null || responce.length() == 0) {
            return null;
        }
        Object responceObj = GwtUtil.decodeResponce(request, responce, targetUri);
        Map args = new HashMap();
        args.put(JsonWriter.PRETTY_PRINT, true);        
        String json = JsonWriter.objectToJson(responceObj, args);
        return json;
    }    
    
    private FileInfo saveFile(byte[] buffer, long contentLength, String contentEncoding, String contentType) throws UnsupportedOperationException, IOException {
        FileInfo fileInfo = new FileInfo();

        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        MultipartStream multipartStream = new MultipartStream(stream, getBoundary(contentType));
        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
            String header = multipartStream.readHeaders();

            /* Разбираем такой заголовок
              Content-Disposition: form-data; name="fileUpload"; filename="Desert.jpg"
              Content-Type: image/jpeg
             */
            Pattern pattern = Pattern.compile("name=\"([^\"]*)\";\\W*filename=\"([^\"]*)\"\\W*Content-Type: ([\\w/]*)");
            Matcher matcher = pattern.matcher(header);
            if (matcher.find()) {
                fileInfo.setParamName(matcher.group(1));
                fileInfo.setFileName(matcher.group(2));
                fileInfo.setFileContentType(matcher.group(3));
            } else {
                fileInfo.setParamName("fileUpload");
                fileInfo.setFileName("test.jpg");
                fileInfo.setFileContentType("image/jpeg");
            }

            FileOutputStream fileStream = new FileOutputStream(fileInfo.getFileName());
            multipartStream.readBodyData(fileStream);
            nextPart = multipartStream.readBoundary();
            fileStream.close();
        }
        return fileInfo;
    }

    private byte[] getBoundary(String contentType) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] { ';',',' });
        String boundaryStr = params.get("boundary");

        if (boundaryStr == null) {
            return null;
        }
        byte[] boundary;
        try {
            boundary = boundaryStr.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            boundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
        }
        return boundary;
    }

    /**
     * Проверка надо ли сохранять запросы, сохраняем только RPC вызовы и вызовы
     * на upload контента
     * @param requestResponce
     * @return
     */
    private boolean isSaveRequest(GwtInteraction requestResponce) {
        boolean result = false;
        if (requestResponce.getRequest().getContentType() != null && requestResponce.getRequest().getContentType().contains("x-gwt-rpc")
                || requestResponce.getResponce().getContentType() != null && requestResponce.getResponce().getContentType().contains("x-gwt-rpc")) {
            result = true;
        } else if (requestResponce.getRequest().getContentType() != null && requestResponce.getRequest().getContentType().contains("multipart/form-data")
                || requestResponce.getResponce().getContentType() != null && requestResponce.getResponce().getContentType().contains("multipart/form-data")) {
            result = true;
        }
        return result;
    }

    private String getBody(byte[] buffer, long contentLength, String contentEncoding) throws UnsupportedOperationException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip") && isGzipStream(buffer)) {
            try {
                GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(buffer));
                byte[] b = new byte[1024];
                int length;
                while ((length = gzis.read(b)) > 0) {
                    out.write(b, 0, length);
                }
            } catch (ZipException ex) {
                // Игнорируем ошибку, по сути нам не нужен ответ для дальнейшей работы;
            }
        } else {
            out.write(buffer);
        }
        return new String(out.toByteArray(), "UTF8");
    }

    public static boolean isGzipStream(byte[] bytes) {
        int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
        return (GZIPInputStream.GZIP_MAGIC == head);
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public GwtRpcJournal getJournal() {
        return journal;
    }

    public int getGroupPause() {
        return groupPause;
    }

    public void setGroupPause(int groupPause) {
        this.groupPause = groupPause;
    }
}
