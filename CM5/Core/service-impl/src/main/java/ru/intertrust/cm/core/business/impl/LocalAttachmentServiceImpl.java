package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.rmi.RemoteException;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * Created by andrey on 24.04.14.
 */
@Stateless
@Local(AttachmentService.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
public class LocalAttachmentServiceImpl extends BaseAttachmentServiceImpl implements AttachmentService {

    @Override
    protected RemoteInputStream wrapStream(InputStream inputStream) throws RemoteException {
        return new DirectRemoteInputStream(inputStream, false);
    }
}
