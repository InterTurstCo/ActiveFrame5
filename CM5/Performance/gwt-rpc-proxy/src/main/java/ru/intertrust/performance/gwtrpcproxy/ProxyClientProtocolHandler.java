package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClientProtocolHandler extends HttpAsyncRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ProxyClientProtocolHandler.class);

    public ProxyClientProtocolHandler() {
        super();
    }

    @Override
    protected void log(final Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void connected(final NHttpClientConnection conn,
            final Object attachment) throws IOException, HttpException {
        logger.info("[proxy->origin] connection open " + conn);
        super.connected(conn, attachment);
    }

    @Override
    public void closed(final NHttpClientConnection conn) {
        logger.info("[proxy->origin] connection closed " + conn);
        super.closed(conn);
    }

}
