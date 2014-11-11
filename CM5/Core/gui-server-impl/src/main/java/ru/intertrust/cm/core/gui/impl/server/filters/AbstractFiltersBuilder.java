package ru.intertrust.cm.core.gui.impl.server.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:57
 */
public abstract class AbstractFiltersBuilder {

    @Autowired
    protected LiteralFieldValueParser literalFieldValueParser;

    @Autowired
    protected CurrentUserAccessor currentUserAccessor;

    @Autowired
    protected ApplicationContext applicationContext;
}
