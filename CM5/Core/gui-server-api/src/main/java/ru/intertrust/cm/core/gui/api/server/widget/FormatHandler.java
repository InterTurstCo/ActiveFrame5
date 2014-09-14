package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public interface FormatHandler extends ComponentHandler {
    static final String FIELD_PLACEHOLDER_PATTERN = "\\{[\\w+\\.*+\\|*+\\w+]+\\}";
    static final Pattern pattern = Pattern.compile(FIELD_PLACEHOLDER_PATTERN);
    String format(IdentifiableObject identifiableObject, Matcher matcher, FormattingConfig formattingConfig);
    String format(DomainObject domainObject, Matcher matcher, FormattingConfig formattingConfig);
    String format(WidgetContext context, Matcher matcher, FormattingConfig formattingConfig);
    String format(Value value, Matcher matcher, FormattingConfig formattingConfig);
    String format(DateBoxState state, Matcher matcher, FormattingConfig formattingConfig);
    String format(String selectionPattern, List<Id> ids, FormattingConfig formattingConfig);

}
