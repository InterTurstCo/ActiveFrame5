package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.model.FatalException;

@Stateless
@Local(ReportService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ReportServiceLocalImpl extends ReportServiceImpl {

    @Override
    protected RemoteInputStream getReportStream(InputStream report) {
        try {
            return new DirectRemoteInputStream(report, false);
        } catch (Exception ex) {
            throw new FatalException("Error get getReportStream", ex);
        }
    }

}
