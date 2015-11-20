package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyResponseConsumer implements HttpAsyncResponseConsumer<ProxyHttpExchange> {
    private static final Logger logger = LoggerFactory.getLogger(ProxyResponseConsumer.class);

    private final ProxyHttpExchange httpExchange;

    private volatile boolean completed;

    public ProxyResponseConsumer(final ProxyHttpExchange httpExchange) {
        super();
        this.httpExchange = httpExchange;
    }

    public void close() throws IOException {
    }

    public void responseReceived(final HttpResponse response) throws IOException {
        try {
            synchronized (this.httpExchange) {
                logger.info("[proxy<-origin] " + this.httpExchange.getId() + " " + response.getStatusLine());
                this.httpExchange.setResponse(response);
                HttpAsyncExchange responseTrigger = this.httpExchange.getResponseTrigger();
                if (responseTrigger != null && !responseTrigger.isCompleted()) {
                    logger.info("[client<-proxy] " + this.httpExchange.getId() + " response triggered");
                    responseTrigger.submitResponse(new ProxyResponseProducer(this.httpExchange));
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public void consumeContent(
            final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setOriginIOControl(ioctrl);
            // Receive data from the origin
            ByteBuffer buf = this.httpExchange.getOutBuffer();
            int startPosition = buf.position();

            int n = decoder.read(buf);

            //Сохранение контента
            saveContent(buf, startPosition, n);

            logger.info("[proxy<-origin] " + this.httpExchange.getId() + " " + n + " bytes read");
            if (decoder.isCompleted()) {
                logger.info("[proxy<-origin] " + this.httpExchange.getId() + " content fully read");
            }
            // If the buffer is full, suspend origin input until there is free
            // space in the buffer
            if (!buf.hasRemaining()) {
                ioctrl.suspendInput();
                logger.info("[proxy<-origin] " + this.httpExchange.getId() + " suspend origin input");
            }
            // If there is some content in the input buffer make sure client
            // output is active
            if (buf.position() > 0) {
                if (this.httpExchange.getClientIOControl() != null) {
                    this.httpExchange.getClientIOControl().requestOutput();
                    logger.info("[proxy<-origin] " + this.httpExchange.getId() + " request client output");
                }
            }
        }
    }

    /**
     * Сохранение контента в буфере
     * @param buf
     * @throws IOException
     */
    private void saveContent(ByteBuffer buf, int startPosition, int length) throws IOException {
        logger.info("[my proxy<-origin] buf.position() " + buf.position() + " buf.limit() " + buf.limit());
        buf.position(startPosition);
        byte[] buffer = new byte[length];
        buf.get(buffer);
        httpExchange.getOutStream().write(buffer);
        logger.info("[my proxy<-origin] write " + length + " bytes to buffer. new size=" + httpExchange.getInStream().size());
    }

    public void responseCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            if (this.completed) {
                return;
            }
            this.completed = true;
            logger.info("[proxy<-origin] " + this.httpExchange.getId() + " response completed");
            this.httpExchange.setResponseReceived();
            if (this.httpExchange.getClientIOControl() != null) {
                this.httpExchange.getClientIOControl().requestOutput();
                logger.info("[proxy<-origin] " + this.httpExchange.getId() + " request client output");
            }
        }
    }

    public void failed(final Exception ex) {
        synchronized (this.httpExchange) {
            if (this.completed) {
                return;
            }
            this.completed = true;
            this.httpExchange.setException(ex);
            HttpAsyncExchange responseTrigger = this.httpExchange.getResponseTrigger();
            if (responseTrigger != null && !responseTrigger.isCompleted()) {
                logger.error("[client<-proxy] Error " + this.httpExchange.getId(), ex);
                int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0, status,
                        EnglishReasonPhraseCatalog.INSTANCE.getReason(status, Locale.US));
                String message = ex.getMessage();
                if (message == null) {
                    message = "Unexpected error";
                }
                response.setEntity(new NStringEntity(message, ContentType.DEFAULT_TEXT));
                responseTrigger.submitResponse(new BasicAsyncResponseProducer(response));
            }
        }
    }

    public boolean cancel() {
        synchronized (this.httpExchange) {
            if (this.completed) {
                return false;
            }
            failed(new InterruptedIOException("Cancelled"));
            return true;
        }
    }

    public ProxyHttpExchange getResult() {
        return this.httpExchange;
    }

    public Exception getException() {
        return null;
    }

    public boolean isDone() {
        return this.completed;
    }

}
