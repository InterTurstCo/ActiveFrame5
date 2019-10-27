package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

@Stateless(name = "ReportService")
@Remote(ReportService.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ReportServiceRemoteImpl extends ReportServiceImpl {

    @Override
    protected RemoteInputStream getReportStream(InputStream report) {
        try {
            SimpleRemoteInputStream remoteInputStream = new SimpleRemoteInputStream(report);
            return remoteInputStream.export();
        } catch (Exception ex) {
            throw new FatalException("Error get getReportStream", ex);
        }
    }

}
