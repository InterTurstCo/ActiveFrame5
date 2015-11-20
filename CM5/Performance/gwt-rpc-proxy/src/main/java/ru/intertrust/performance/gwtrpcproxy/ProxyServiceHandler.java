package ru.intertrust.performance.gwtrpcproxy;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.protocol.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServiceHandler extends HttpAsyncService {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServiceHandler.class);

    public ProxyServiceHandler(
            final HttpProcessor httpProcessor,
            final ConnectionReuseStrategy reuseStrategy,
            final HttpAsyncRequestHandlerMapper handlerResolver) {
        super(httpProcessor, reuseStrategy, null, handlerResolver, null);
    }

    @Override
    protected void log(final Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void connected(final NHttpServerConnection conn) {
        logger.info("[client->proxy] connection open " + conn);
        super.connected(conn);
    }

    @Override
    public void closed(final NHttpServerConnection conn) {
        logger.info("[client->proxy] connection closed " + conn);
        super.closed(conn);
    }

}
