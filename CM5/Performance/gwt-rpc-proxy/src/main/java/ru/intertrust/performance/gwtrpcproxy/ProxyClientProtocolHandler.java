package ru.intertrust.performance.gwtrpcproxy;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;

public class ProxyClientProtocolHandler extends HttpAsyncRequestExecutor {

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
        System.out.println("[proxy->origin] connection open " + conn);
        super.connected(conn, attachment);
    }

    @Override
    public void closed(final NHttpClientConnection conn) {
        System.out.println("[proxy->origin] connection closed " + conn);
        super.closed(conn);
    }

}
