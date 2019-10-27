package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.06.2015
 *         Time: 9:07
 */
@Stateless(name = "UserSettingsFetcher")
@Local(UserSettingsFetcher.class)
@Remote(UserSettingsFetcher.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
public class UserSettingsFetcherImpl implements UserSettingsFetcher {
    @Autowired
    private CurrentUserAccessor currentUserAccessor;
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private CrudService crudService;

    public DomainObject getUserSettingsDomainObject(boolean lock) {
        final IdentifiableObject identifiableObject =
                getUserSettingsIdentifiableObject(currentUserAccessor.getCurrentUser(), collectionsService);
        final DomainObject result;
        if (identifiableObject != null) {
            result = lock ? crudService.findAndLock(identifiableObject.getId()) : crudService.find(identifiableObject.getId());
        } else {
            result = crudService.createDomainObject("bu_user_settings");
            result.setReference("person", currentUserAccessor.getCurrentUserId());
        }
        return result;
    }

    @Override
    public DomainObject getUserHipSettingsDomainObject(String pId) {
        DomainObject result;
        IdentifiableObject identifiableObject = getUserSettingsIdentifiableObject(pId);
        if (identifiableObject != null) {
            result = crudService.find(identifiableObject.getId());
        } else {
            result = crudService.createDomainObject("bu_user_hip_settings");
            result.setReference("person", currentUserAccessor.getCurrentUserId());
            result.setString("plugin_id",pId);
        }
        return result;
    }

    private IdentifiableObject getUserSettingsIdentifiableObject(final String userLogin,
                                                                 final CollectionsService collectionsService) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byPerson", 0, new StringValue(userLogin)));
        final IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_user_settings_collection", null, filters);
        return collection.size() == 0 ? null : collection.get(0);
    }

    private IdentifiableObject getUserSettingsIdentifiableObject(String pId) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byPerson", 0, new StringValue(currentUserAccessor.getCurrentUser())));
        filters.add(Filter.create("byPid", 1, new StringValue(pId)));
        final IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_user_hip_settings_collection", null, filters);
        return collection.size() == 0 ? null : collection.get(0);
    }


}
