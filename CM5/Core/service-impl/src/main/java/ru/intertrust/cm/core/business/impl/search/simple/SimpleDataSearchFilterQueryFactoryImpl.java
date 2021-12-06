package ru.intertrust.cm.core.business.impl.search.simple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.model.FatalException;

@Service
public class SimpleDataSearchFilterQueryFactoryImpl implements SimpleDataSearchFilterQueryFactory {

    private final Map<Class<?>, SimpleDataSearchFilterQueryService> services;

    @Autowired
    public SimpleDataSearchFilterQueryFactoryImpl(List<SimpleDataSearchFilterQueryService> services) {
        this.services = services.stream()
                .collect(Collectors.toMap(SimpleDataSearchFilterQueryService::getType, it -> it));
    }

    @Override
    public String getQuery(SimpleDataConfig config, SimpleDataSearchFilter filter) {
        SimpleDataSearchFilterQueryService service = services.get(filter.getClass());
        if (service == null) {
            throw new FatalException("Filter " + filter.getClass() + " is not supported");
        }

        return service.prepareQuery(config, filter);
    }
}
