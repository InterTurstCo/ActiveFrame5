package ru.intertrust.cm.plugins.permission;

import java.util.Set;

import javax.ejb.EJBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.model.FatalException;

@Plugin(name="RecalcAllGroupGroupPlugin", description="Пересчет состава всех групп в системе из иерархической в плоскую", transactional = false)
public class RecalcAllGroupGroupPlugin implements PluginHandler {
    private static final Logger logger = LoggerFactory.getLogger(RecalcAllGroupGroupPlugin.class);
    
    @Autowired
    private PersonManagementServiceDao personManagementService;
    
    @Override
    public String execute(EJBContext context, String param) {
        logger.info("Start plugin RecalcAllGroupGroupPlugin");
        Set<Id> result = personManagementService.getAllRootGroup();
        logger.info("Found {} root groups", result.size());
        for (Id id : result) {
            try{
                context.getUserTransaction().begin();
                logger.info("Recalc group {}" + id);
                personManagementService.recalcGroupGroupForGroupAndChildGroups(id);
                context.getUserTransaction().commit();
            }catch(Exception ex){
                try{
                    context.getUserTransaction().rollback();
                }catch(Exception ignoreEx){
                }
                throw new FatalException("Error on run RecalcAllGroupGroupPlugin plugin", ex);
            }
        }
        logger.info("Finish plugin RecalcAllGroupGroupPlugin");
        return "Пересчитан состав " + result.size() + " групп";
    }
}
