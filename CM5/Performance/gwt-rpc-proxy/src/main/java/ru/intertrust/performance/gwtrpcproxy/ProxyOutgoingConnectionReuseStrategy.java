package ru.intertrust.performance.gwtrpcproxy;

import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

public class ProxyOutgoingConnectionReuseStrategy extends DefaultConnectionReuseStrategy {

    @Override
    public boolean keepAlive(final HttpResponse response, final HttpContext context) {
        NHttpConnection conn = (NHttpConnection) context.getAttribute(
                HttpCoreContext.HTTP_CONNECTION);
        boolean keepAlive = super.keepAlive(response, context);
        if (keepAlive) {
            System.out.println("[proxy->origin] connection kept alive " + conn);
        }
        return keepAlive;
    }

}
