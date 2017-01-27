package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.model.FatalException;

@Stateless(name = "ReportService")
@Remote(ReportService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
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
