package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.rmi.RemoteException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * User: vlad
 */
@Stateless
@Remote(AttachmentService.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
public class RemoteAttachmentServiceImpl extends BaseAttachmentServiceImpl implements AttachmentService.Remote {
    @Override
    protected RemoteInputStream wrapStream(InputStream inputStream) throws RemoteException {
        SimpleRemoteInputStream remoteInputStream = new SimpleRemoteInputStream(inputStream);
        return remoteInputStream.export();
    }
}