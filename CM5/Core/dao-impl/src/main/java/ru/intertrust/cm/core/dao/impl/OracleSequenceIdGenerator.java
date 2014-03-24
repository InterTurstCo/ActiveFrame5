package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Oracle-специфичная имплементация {@link ru.intertrust.cm.core.dao.api.IdGenerator }
 * @author vmatsukevich
 *
 */
public class OracleSequenceIdGenerator extends BasicSequenceIdGenerator {

    @Override
    protected String generateSelectNextValueQuery(String sequenceName) {
        return "select " + wrap(sequenceName) + ".nextval from dual";
    }
}
