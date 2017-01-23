package ru.intertrust.cm.core.business.impl;

import java.io.File;
import java.io.FileInputStream;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStream;

import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.model.FatalException;

@Stateless
@Local(ReportService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceLocalImpl extends ReportServiceImpl {

    @Override
    protected RemoteInputStream getReportStream(File report) {
        try {
            return new DirectRemoteInputStream(new FileInputStream(report), false);
        } catch (Exception ex) {
            throw new FatalException("Error get getReportStream", ex);
        }
    }

}
