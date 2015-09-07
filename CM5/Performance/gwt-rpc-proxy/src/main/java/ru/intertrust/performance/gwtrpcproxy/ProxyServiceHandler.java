package ru.intertrust.performance.gwtrpcproxy;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerMapper;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.protocol.HttpProcessor;

public class ProxyServiceHandler extends HttpAsyncService {

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
        System.out.println("[client->proxy] connection open " + conn);
        super.connected(conn);
    }

    @Override
    public void closed(final NHttpServerConnection conn) {
        System.out.println("[client->proxy] connection closed " + conn);
        super.closed(conn);
    }

}
