package ru.intertrust.cm.core.business.api.impl;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;

/**
 * {@inheritDoc}
 */
@Service
public class RdbmsIdServiceImpl implements IdService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Id createId(String stringRep) {
        return new RdbmsId(stringRep);
    }
}