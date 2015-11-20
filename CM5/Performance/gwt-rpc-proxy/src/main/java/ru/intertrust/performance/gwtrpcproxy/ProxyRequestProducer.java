package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyRequestProducer implements HttpAsyncRequestProducer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestProducer.class);

    private final ProxyHttpExchange httpExchange;

    public ProxyRequestProducer(final ProxyHttpExchange httpExchange) {
        super();
        this.httpExchange = httpExchange;
    }

    public void close() throws IOException {
    }

    public HttpHost getTarget() {
        synchronized (this.httpExchange) {
            return this.httpExchange.getTarget();
        }
    }

    public HttpRequest generateRequest() {
        try {

            synchronized (this.httpExchange) {
                HttpRequest request = this.httpExchange.getRequest();
                logger.info("[proxy->origin] " + this.httpExchange.getId() + " " + request.getRequestLine());
                // Rewrite request!!!!
                HttpRequest result = null;
                if (request instanceof HttpEntityEnclosingRequest) {
                    result = new BasicHttpEntityEnclosingRequest(
                            request.getRequestLine());
                    ((BasicHttpEntityEnclosingRequest)result).setEntity(((HttpEntityEnclosingRequest) request).getEntity());
                } else {
                    result = new BasicHttpRequest(request.getRequestLine());                        
                }

                for (Header header : request.getAllHeaders()) {
                    if (!header.getName().equalsIgnoreCase("Content-Length")) {
                        if (header.getName().equalsIgnoreCase(HTTP.TARGET_HOST)) {
                            result.setHeader(replaceHostHeader(header));
                        }else if(header.getName().equalsIgnoreCase("X-GWT-Module-Base")){
                            result.setHeader(replaceUriHeader(header));
                        }else if(header.getName().equalsIgnoreCase("Origin")){
                            result.setHeader(replaceUriHeader(header));
                        }else if(header.getName().equalsIgnoreCase("Referer")){
                            result.setHeader(replaceUriHeader(header));
                        }else {
                            result.setHeader(header);
                        }
                    }
                }
                logger.info("[proxy->origin] origin " + request);
                logger.info("[proxy->origin] proxy " + result);
                return result;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setOriginIOControl(ioctrl);
            // Send data to the origin server
            ByteBuffer buf = this.httpExchange.getInBuffer();
            buf.flip();
            int n = encoder.write(buf);
            buf.compact();
            logger.info("[proxy->origin] " + this.httpExchange.getId() + " " + n + " bytes written");
            // If there is space in the buffer and the message has not been
            // transferred, make sure the client is sending more data
            if (buf.hasRemaining() && !this.httpExchange.isRequestReceived()) {
                if (this.httpExchange.getClientIOControl() != null) {
                    this.httpExchange.getClientIOControl().requestInput();
                    logger.info("[proxy->origin] " + this.httpExchange.getId() + " request client input");
                }
            }
            if (buf.position() == 0) {
                if (this.httpExchange.isRequestReceived()) {
                    encoder.complete();
                    logger.info("[proxy->origin] " + this.httpExchange.getId() + " content fully written");
                } else {
                    // Input buffer is empty. Wait until the client fills up
                    // the buffer
                    ioctrl.suspendOutput();
                    logger.info("[proxy->origin] " + this.httpExchange.getId() + " suspend origin output");
                }
            }
        }
    }

    private Header replaceUriHeader(Header header) throws URISyntaxException {
        URI targetUri = new URI(this.httpExchange.getProxyContext().getTargetUri());
        String location = header.getValue();
        URI uri = new URI(location);
        URI newUri =
                new URI(uri.getScheme(), uri.getUserInfo(), targetUri.getHost(), targetUri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        Header newHeader = new BasicHeader(header.getName(), newUri.toString());
        return newHeader;
    }

    private Header replaceHostHeader(Header header) throws URISyntaxException {
        URI targetUri = new URI(this.httpExchange.getProxyContext().getTargetUri());
        Header newHeader = new BasicHeader(header.getName(), targetUri.getHost() + ":" + targetUri.getPort());
        return newHeader;
    }
    
    
    public void requestCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            logger.info("[proxy->origin] " + this.httpExchange.getId() + " request completed");
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public void resetRequest() {
    }

    public void failed(final Exception ex) {
        logger.error("[proxy->origin] Error", ex);
    }

}