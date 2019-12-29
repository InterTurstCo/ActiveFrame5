package ru.intertrust.cm.core.gui.impl.server.cmd.impl;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.gui.impl.server.cmd.PlatformCommandLineService;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebService;
import ru.intertrust.cm.core.gui.impl.server.cmd.model.PlatformWebServiceResult;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.List;
import java.util.Map;

@Stateless(name = "PlatformCommandLineService")
@Local(PlatformCommandLineService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PlatformCommandLineServiceImpl implements PlatformCommandLineService {

    @Autowired
    private ApplicationContext context;

    @Override
    public PlatformWebServiceResult execute(String beanName, List<FileItem> files, Map<String, String[]> params) {
        PlatformWebService execBean = (PlatformWebService)context.getBean(beanName);
        return execBean.execute(files, params);
    }
}
