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

    @Override
    protected String generateSelectNextValuesQuery(String sequenceName, Integer nextValuesNumber) {
        if (nextValuesNumber == null || nextValuesNumber < 1) {
            throw new IllegalArgumentException("nextValuesNumber must be positive integer");
        }

        return "select " + wrap(sequenceName) + ".nextval from dual connect by level<=" + nextValuesNumber;
    }
}
