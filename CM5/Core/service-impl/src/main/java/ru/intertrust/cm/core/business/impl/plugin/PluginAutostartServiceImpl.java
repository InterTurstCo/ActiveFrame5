package ru.intertrust.cm.core.business.impl.plugin;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.plugin.PluginAutostartService;
import ru.intertrust.cm.core.business.api.plugin.PluginService;

@Stateless(name = "PluginAutostartService")
@Local(PluginAutostartService.class)
@javax.ejb.Remote(PluginAutostartService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@RunAs("system")
public class PluginAutostartServiceImpl implements PluginAutostartService, PluginAutostartService.Remote{
    @Resource
    private EJBContext ejbContext;
    
    @Autowired
    private PluginService pluginService;
    
    @Override
    @RolesAllowed("system")
    public String execute(String id, String param) {
        return pluginService.executePlugin(id, param);
    }

}
