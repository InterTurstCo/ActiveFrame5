package ru.intertrust.cm.core.business.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.load.ImportData;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

@Stateless(name = "ImportDataService")
@Local(ImportDataService.class)
@Remote(ImportDataService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ImportDataServiceImpl implements ImportDataService {

    @Autowired
    private ApplicationContext springContext;
    @Resource
    private EJBContext context;

    @Override
    public List<Id> importData(byte[] importFileAsByteArray) {
        return importData(importFileAsByteArray, null);
    }

    @Override
    public List<Id> importData(byte[] importFileAsByteArray, String encoding) {
        return importData(importFileAsByteArray, encoding, false);
    }

    @Override
    public List<Id> importData(byte[] importFileAsByteArray, String encoding,
            boolean owerwrite) {
        return importData(importFileAsByteArray, encoding, owerwrite, null);
    }

    @Override
    public List<Id> importData(byte[] importFileAsByteArray, String encoding, boolean owerwrite, String attachmentBasePath) {
        try {
            ImportData importData = (ImportData) springContext.getBean(ImportData.PERSON_IMPORT_BEAN);
            return importData.importData(importFileAsByteArray, encoding, owerwrite, attachmentBasePath, context.getUserTransaction());
        } catch (Exception ex) {
            throw new FatalException("Error load data", ex);
        }
    }
}
