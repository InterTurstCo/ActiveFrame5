package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public interface FormatHandler extends ComponentHandler {
    public static final String FIELD_PLACEHOLDER_PATTERN = "\\{[\\w+\\.*+\\|*+\\w+]+\\}";
    public static final Pattern pattern = Pattern.compile(FIELD_PLACEHOLDER_PATTERN);
    public String format(IdentifiableObject identifiableObject, Matcher matcher);
    public String format(DomainObject domainObject, Matcher matcher);
    public String format(WidgetContext context, Matcher matcher, String allValuesEmpty);

}
