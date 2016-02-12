package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.AttachmentService;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * Created by andrey on 24.04.14.
 */
@Stateless
@Local(AttachmentService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class LocalAttachmentServiceImpl extends BaseAttachmentServiceImpl implements AttachmentService {

    @Override
    protected RemoteInputStream wrapStream(InputStream inputStream) throws RemoteException {
        return new DirectRemoteInputStream(inputStream, false);
    }
}
