package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.impl.server.form.FormRetriever;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.transaction.*;
import java.util.List;

import static ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper.getUserSettingsIdentifiableObject;

/**
 * Created by Ravil on 16.01.2018.
 */
@Stateless(name = "settingsUtilImpl")
@Local(SettingsUtil.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@RunAs("system")
@TransactionManagement(TransactionManagementType.BEAN)
public class SettingsUtilImpl implements SettingsUtil {

    private static final String SETTINGS_OBJECT_DO = "bu_nav_link_collection";

    private static Logger log = LoggerFactory.getLogger(FormRetriever.class);
    @Resource
    private EJBContext ejbContext;

    @Autowired private
    CurrentUserAccessor currentUserAccessor;

    @Autowired private
    CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Override
    public void deleteIds(List<Id> ids) {
        if(!ids.isEmpty()) {
            try {
                ejbContext.getUserTransaction().begin();
                int deleted = crudService.delete(ids);
                log.debug("SettingsUtilImpl.deleteIds removed: " + deleted);
                ejbContext.getUserTransaction().commit();
            } catch (NotSupportedException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            } catch (HeuristicMixedException e) {
                e.printStackTrace();
            } catch (HeuristicRollbackException e) {
                e.printStackTrace();
            } catch (RollbackException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteIds(List<Id> ids, AccessToken token) {
        if(!ids.isEmpty()) {
            try {
                ejbContext.getUserTransaction().begin();
                int deleted = domainObjectDao.delete(ids,token);
                log.debug("SettingsUtilImpl.deleteIds(with token) removed: " + deleted);
                ejbContext.getUserTransaction().commit();
            } catch (NotSupportedException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            } catch (HeuristicMixedException e) {
                e.printStackTrace();
            } catch (HeuristicRollbackException e) {
                e.printStackTrace();
            } catch (RollbackException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void saveTheme(String theme) {
        try {
            ejbContext.getUserTransaction().begin();

            IdentifiableObject identifiableObject =
                    getUserSettingsIdentifiableObject(currentUserAccessor.getCurrentUser(), collectionsService);
            DomainObject result;
            if (identifiableObject != null) {
                result = crudService.find(identifiableObject.getId());
            } else {
                result = crudService.createDomainObject("bu_user_settings");
                result.setReference("person", currentUserAccessor.getCurrentUserId());
            }
            result.setString(UserSettingsHelper.DO_THEME_FIELD_KEY, theme);
            crudService.save(result);
            ejbContext.getUserTransaction().commit();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DomainObject createNewObject(String link, Id person, Long count, String vName) {
        DomainObject newObject = null;
        try {
            ejbContext.getUserTransaction().begin();
            newObject = crudService.createDomainObject(SETTINGS_OBJECT_DO);
            newObject.setString("link", link);
            newObject.setReference("person", person);
            newObject.setLong("collection_count", count);
            if(vName!=null){
                newObject.setValue("collection_view_name", new StringValue(vName));
            }
            newObject = crudService.save(newObject);
            log.debug("SettingsUtilImpl.createNewObject created object: " + newObject.getId());
            ejbContext.getUserTransaction().commit();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        }
        return newObject;
    }

    @Override
    public void saveCounter(Id id, Long counter) {
        try {
            ejbContext.getUserTransaction().begin();
            DomainObject navLinkCollectionObject = crudService.find(id);
            if(!counter.equals(navLinkCollectionObject.getLong("collection_count"))){
                navLinkCollectionObject.setLong("collection_count", counter);
                navLinkCollectionObject = crudService.save(navLinkCollectionObject);
            }
            log.debug("SettingsUtilImpl.saveCounter saved object: " + navLinkCollectionObject.getId());
            ejbContext.getUserTransaction().commit();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        }
    }


}
