package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyRequestHandler implements HttpAsyncRequestHandler<ProxyHttpExchange> {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestHandler.class);

    private final HttpHost target;
    private final HttpAsyncRequester executor;
    private final BasicNIOConnPool connPool;
    private final AtomicLong counter;
    private ProxyContext proxyContext;

    public ProxyRequestHandler(
            final HttpHost target,
            final HttpAsyncRequester executor,
            final BasicNIOConnPool connPool, 
            ProxyContext context) {
        super();
        this.target = target;
        this.executor = executor;
        this.connPool = connPool;
        this.counter = new AtomicLong(1);
        this.proxyContext = context;
    }

    public HttpAsyncRequestConsumer<ProxyHttpExchange> processRequest(
            final HttpRequest request,
            final HttpContext context) {
        ProxyHttpExchange httpExchange = (ProxyHttpExchange) context.getAttribute("http-exchange");
        if (httpExchange == null) {
            httpExchange = new ProxyHttpExchange(proxyContext);
            context.setAttribute("http-exchange", httpExchange);
        }
        synchronized (httpExchange) {
            httpExchange.reset();
            String id = String.format("%08X", this.counter.getAndIncrement());
            httpExchange.setId(id);
            httpExchange.setTarget(this.target);
            return new ProxyRequestConsumer(httpExchange, this.executor, this.connPool);
        }
    }

    public void handle(
            final ProxyHttpExchange httpExchange,
            final HttpAsyncExchange responseTrigger,
            final HttpContext context) throws HttpException, IOException {
        synchronized (ProxyRequestHandler.class) {
            Exception ex = httpExchange.getException();
            if (ex != null) {                
                int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0, status,
                        EnglishReasonPhraseCatalog.INSTANCE.getReason(status, Locale.US));
                String message = ex.getMessage();
                if (message == null) {
                    message = "Unexpected error";
                }
                response.setEntity(new NStringEntity(message, ContentType.DEFAULT_TEXT));
                responseTrigger.submitResponse(new BasicAsyncResponseProducer(response));
                logger.error("[client<-proxy] error response triggered " + httpExchange.getId(), ex);
            }
            HttpResponse response = httpExchange.getResponse();
            if (response != null) {
                responseTrigger.submitResponse(new ProxyResponseProducer(httpExchange));
                logger.info("[client<-proxy] " + httpExchange.getId() + " response triggered");
            }
            // No response yet.
            httpExchange.setResponseTrigger(responseTrigger);
        }
    }

}