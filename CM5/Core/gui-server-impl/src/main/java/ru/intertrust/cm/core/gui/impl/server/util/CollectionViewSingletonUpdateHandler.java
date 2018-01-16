package ru.intertrust.cm.core.gui.impl.server.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.event.SingletonConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.gui.impl.server.action.system.SettingsUtil;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Обработчик изменения конфигурации {@link CollectionViewConfig}. Выполняется один раз на кластер экземпляров приложений
 */
public class CollectionViewSingletonUpdateHandler implements ApplicationListener<SingletonConfigurationUpdateEvent> {

    @Autowired
    private CollectionsDao collectionsDao;

    @Autowired
    private AccessControlService accessControlService;

    @EJB
    SettingsUtil settingsUtil;

    @Override
    public void onApplicationEvent(SingletonConfigurationUpdateEvent event) {
        final ConfigurationUpdateEvent original = event.getOriginal();
        if (!original.configTypeChanged(CollectionViewConfig.class)) {
            return;
        }
        final Set<String> changedConfigNames = original.getChangedConfigNames(CollectionViewConfig.class);
        for (String changedCollectionView : changedConfigNames) {
            final AccessToken token = accessControlService.createSystemAccessToken("CollectionViewSingletonUpdateHandler");
            final List<Filter> filters = new ArrayList<>();
            filters.add(Filter.create("byCollectionViewName", 0, new StringValue(changedCollectionView)));
            final IdentifiableObjectCollection collection =
                    collectionsDao.findCollection("bu_nav_link_collections", filters, null, 0, 0, token);
            ArrayList<Id> toDelete = new ArrayList<>(collection.size());
            for (int i = 0; i < collection.size(); ++i) {
                toDelete.add(collection.getId(i));
            }
            settingsUtil.deleteIds(toDelete,token);
        }
    }
}
