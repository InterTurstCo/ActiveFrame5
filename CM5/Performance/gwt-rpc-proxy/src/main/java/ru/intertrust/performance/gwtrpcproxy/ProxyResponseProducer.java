package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncResponseProducer;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public  class ProxyResponseProducer implements HttpAsyncResponseProducer {

    private final ProxyHttpExchange httpExchange;

    public ProxyResponseProducer(final ProxyHttpExchange httpExchange) {
        super();
        this.httpExchange = httpExchange;
    }

    public void close() throws IOException {
        this.httpExchange.reset();
    }

    public HttpResponse generateResponse() {
        try {
            synchronized (this.httpExchange) {
                HttpResponse response = this.httpExchange.getResponse();
                System.out.println("[client<-proxy] " + this.httpExchange.getId() + " " + response.getStatusLine());
                // Rewrite response!!!!
                BasicHttpResponse r = new BasicHttpResponse(response.getStatusLine());
                r.setEntity(response.getEntity());
                for (Header header : response.getAllHeaders()) {
                    if (!(header.getName().equalsIgnoreCase(HTTP.CONTENT_LEN) || header.getName().equalsIgnoreCase(HTTP.TRANSFER_ENCODING))) {
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY && header.getName().equalsIgnoreCase("Location")) {
                            String location = header.getValue();
                            URI uri = new URI(location);
                            URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), "localhost", this.httpExchange.getProxyContext().getLocalPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                            Header newHeader = new BasicHeader(header.getName(), newUri.toString());
                            r.setHeader(newHeader);
                        } else {
                            r.setHeader(header);
                        }
                    }
                }
                System.out.println("[client<-proxy] origin " + response.toString());
                System.out.println("[client<-proxy] proxy " + r.toString());
                return r;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void produceContent(
            final ContentEncoder encoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setClientIOControl(ioctrl);
            // Send data to the client
            ByteBuffer buf = this.httpExchange.getOutBuffer();
            buf.flip();
            int n = encoder.write(buf);
            buf.compact();
            System.out.println("[client<-proxy] " + this.httpExchange.getId() + " " + n + " bytes written");
            // If there is space in the buffer and the message has not been
            // transferred, make sure the origin is sending more data
            if (buf.hasRemaining() && !this.httpExchange.isResponseReceived()) {
                if (this.httpExchange.getOriginIOControl() != null) {
                    this.httpExchange.getOriginIOControl().requestInput();
                    System.out.println("[client<-proxy] " + this.httpExchange.getId() + " request origin input");
                }
            }
            if (buf.position() == 0) {
                if (this.httpExchange.isResponseReceived()) {
                    encoder.complete();
                    System.out.println("[client<-proxy] " + this.httpExchange.getId() + " content fully written");
                } else {
                    // Input buffer is empty. Wait until the origin fills up
                    // the buffer
                    ioctrl.suspendOutput();
                    System.out.println("[client<-proxy] " + this.httpExchange.getId() + " suspend client output");
                }
            }
        }
    }

    public void responseCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            System.out.println("[client<-proxy] " + this.httpExchange.getId() + " response completed");
            httpExchange.getProxyContext().addResponce(httpExchange);
        }
    }

    public void failed(final Exception ex) {
        System.out.println("[client<-proxy] " + ex.toString());
    }

}