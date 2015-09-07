package ru.intertrust.performance.gwtrpcproxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncExchange;

public class ProxyHttpExchange {

    private final ByteBuffer inBuffer;
    private final ByteBuffer outBuffer;

    private volatile String id;
    private volatile HttpHost target;
    private volatile HttpAsyncExchange responseTrigger;
    private volatile IOControl originIOControl;
    private volatile IOControl clientIOControl;
    private volatile HttpRequest request;
    private volatile boolean requestReceived;
    private volatile HttpResponse response;
    private volatile boolean responseReceived;
    private volatile Exception ex;
    private ProxyContext context;
    private ByteArrayOutputStream inStream = new ByteArrayOutputStream(); 
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream(); 

    public ProxyHttpExchange(ProxyContext context) {
        super();
        this.inBuffer = ByteBuffer.allocateDirect(10240);
        this.outBuffer = ByteBuffer.allocateDirect(10240);
        this.context = context;
    }

    public ByteBuffer getInBuffer() {
        return this.inBuffer;
    }

    public ByteBuffer getOutBuffer() {
        return this.outBuffer;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public HttpHost getTarget() {
        return this.target;
    }

    public void setTarget(final HttpHost target) {
        this.target = target;
    }

    public HttpRequest getRequest() {
        return this.request;
    }

    public void setRequest(final HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    public void setResponse(final HttpResponse response) {
        this.response = response;
    }

    public HttpAsyncExchange getResponseTrigger() {
        return this.responseTrigger;
    }

    public void setResponseTrigger(final HttpAsyncExchange responseTrigger) {
        this.responseTrigger = responseTrigger;
    }

    public IOControl getClientIOControl() {
        return this.clientIOControl;
    }

    public void setClientIOControl(final IOControl clientIOControl) {
        this.clientIOControl = clientIOControl;
    }

    public IOControl getOriginIOControl() {
        return this.originIOControl;
    }

    public void setOriginIOControl(final IOControl originIOControl) {
        this.originIOControl = originIOControl;
    }

    public boolean isRequestReceived() {
        return this.requestReceived;
    }

    public void setRequestReceived() {
        this.requestReceived = true;
    }

    public boolean isResponseReceived() {
        return this.responseReceived;
    }

    public void setResponseReceived() {
        this.responseReceived = true;
    }

    public Exception getException() {
        return this.ex;
    }

    public void setException(final Exception ex) {
        this.ex = ex;
    }

    public void reset() {
        this.inBuffer.clear();
        this.outBuffer.clear();
        this.target = null;
        this.id = null;
        this.responseTrigger = null;
        this.clientIOControl = null;
        this.originIOControl = null;
        this.request = null;
        this.requestReceived = false;
        this.response = null;
        this.responseReceived = false;
        this.ex = null;
    }

    public ProxyContext getProxyContext() {
        return context;
    }

    public ByteArrayOutputStream getInStream() {
        return inStream;
    }

    public ByteArrayOutputStream getOutStream() {
        return outStream;
    }
}