package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.HttpRequest;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyRequestConsumer implements HttpAsyncRequestConsumer<ProxyHttpExchange> {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestConsumer.class);

    private final ProxyHttpExchange httpExchange;
    private final HttpAsyncRequester executor;
    private final BasicNIOConnPool connPool;

    private volatile boolean completed;

    public ProxyRequestConsumer(
            final ProxyHttpExchange httpExchange,
            final HttpAsyncRequester executor,
            final BasicNIOConnPool connPool) {
        super();
        this.httpExchange = httpExchange;
        this.executor = executor;
        this.connPool = connPool;
    }

    public void close() throws IOException {
    }

    public void requestReceived(final HttpRequest request) {
        synchronized (this.httpExchange) {
            logger.info("[client->proxy] " + this.httpExchange.getId() + " " + request.getRequestLine());
            if (request.getRequestLine().getMethod().equalsIgnoreCase("GET") && request.getRequestLine().getUri().endsWith("/proxy/shutdown")){
                logger.info("Shutdown by request");
                System.exit(0);
            }
            this.httpExchange.setRequest(request);
            this.executor.execute(
                    new ProxyRequestProducer(this.httpExchange),
                    new ProxyResponseConsumer(this.httpExchange),
                    this.connPool);
        }
    }

    public void consumeContent(
            final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
        synchronized (this.httpExchange) {
            this.httpExchange.setClientIOControl(ioctrl);
            // Receive data from the client
            ByteBuffer buf = this.httpExchange.getInBuffer();
            
            int startPosition = buf.position();
            logger.info("[my client->proxy] buf.position() before read = " + startPosition);
            int n = decoder.read(buf);
            
            //Записываем контент для нужд прокси
            saveContent(buf, startPosition, n);
            
            logger.info("[client->proxy] " + this.httpExchange.getId() + " " + n + " bytes read");
            if (decoder.isCompleted()) {
                logger.info("[client->proxy] " + this.httpExchange.getId() + " content fully read");
            }
            // If the buffer is full, suspend client input until there is free
            // space in the buffer
            if (!buf.hasRemaining()) {
                ioctrl.suspendInput();
                logger.info("[client->proxy] " + this.httpExchange.getId() + " suspend client input");
            }
            // If there is some content in the input buffer make sure origin
            // output is active
            if (buf.position() > 0) {
                if (this.httpExchange.getOriginIOControl() != null) {
                    this.httpExchange.getOriginIOControl().requestOutput();
                    logger.info("[client->proxy] " + this.httpExchange.getId() + " request origin output");
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
        if (httpExchange.getRequest() instanceof BasicHttpEntityEnclosingRequest) {
            logger.info("[my proxy->origin] buf.position() " + buf.position() + " buf.limit() " + buf.limit());
            buf.position(startPosition);
            byte[] buffer = new byte[length];            
            buf.get(buffer);
            httpExchange.getInStream().write(buffer);
            logger.info("[my client->proxy] write " + length + " bytes to buffer. new size=" + httpExchange.getInStream().size());
        }
    }

    public void requestCompleted(final HttpContext context) {
        synchronized (this.httpExchange) {
            this.completed = true;
            logger.info("[client->proxy] " + this.httpExchange.getId() + " request completed");
            this.httpExchange.setRequestReceived();
            if (this.httpExchange.getOriginIOControl() != null) {
                this.httpExchange.getOriginIOControl().requestOutput();
                logger.info("[client->proxy] " + this.httpExchange.getId() + " request origin output");
            }
        }
    }

    public Exception getException() {
        return null;
    }

    public ProxyHttpExchange getResult() {
        return this.httpExchange;
    }

    public boolean isDone() {
        return this.completed;
    }

    public void failed(final Exception ex) {
        logger.error("[client->proxy] Error", ex);
    }

}