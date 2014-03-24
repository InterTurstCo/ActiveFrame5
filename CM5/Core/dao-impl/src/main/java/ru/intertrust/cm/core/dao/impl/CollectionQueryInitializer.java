package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;

import java.util.List;

/**
 * Created by vmatsukevich on 3/19/14.
 */
public interface CollectionQueryInitializer {

    public String initializeQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
                                  SortOrder sortOrder, int offset, int limit, AccessToken accessToken);

    public String initializeQuery(String query, int offset, int limit, AccessToken accessToken);

    public String initializeCountQuery(CollectionConfig collectionConfig, List<? extends Filter> filterValues,
                                       AccessToken accessToken);
}
