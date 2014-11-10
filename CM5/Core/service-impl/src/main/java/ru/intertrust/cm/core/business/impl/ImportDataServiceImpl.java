package ru.intertrust.cm.core.business.impl;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.load.ImportData;
import ru.intertrust.cm.core.model.FatalException;

@Stateless(name = "ImportDataService")
@Local(ImportDataService.class)
@Remote(ImportDataService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ImportDataServiceImpl implements ImportDataService {

    @Autowired
    private ApplicationContext springContext;
    @Resource
    private EJBContext context;

    @Override
    public void importData(byte[] importFileAsByteArray) {
        importData(importFileAsByteArray, null);
    }

    @Override
    public void importData(byte[] importFileAsByteArray, String encoding) {
        importData(importFileAsByteArray, encoding, false);
    }

    @Override
    public void importData(byte[] importFileAsByteArray, String encoding,
            boolean rewrite) {
        try {
            ImportData importData = (ImportData)springContext.getBean(ImportData.PERSON_IMPORT_BEAN);
            importData.importData(importFileAsByteArray, encoding, rewrite);
        } catch (Exception ex) {
            throw new FatalException("Error load data", ex);
        }

    }
}
