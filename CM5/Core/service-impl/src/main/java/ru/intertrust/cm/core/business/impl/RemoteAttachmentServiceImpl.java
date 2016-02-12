package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.AttachmentService;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * User: vlad
 */
@Stateless
@Remote(AttachmentService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class RemoteAttachmentServiceImpl extends BaseAttachmentServiceImpl implements AttachmentService.Remote {
    @Override
    protected RemoteInputStream wrapStream(InputStream inputStream) throws RemoteException {
        SimpleRemoteInputStream remoteInputStream = new SimpleRemoteInputStream(inputStream);
        return remoteInputStream.export();
    }
}