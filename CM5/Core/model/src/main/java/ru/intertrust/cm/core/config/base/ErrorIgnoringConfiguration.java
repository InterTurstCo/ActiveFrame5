package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.ConfigurationConverter;
import ru.intertrust.cm.core.config.converter.ErrorIgnoringConfigurationConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
@Convert(ErrorIgnoringConfigurationConverter.class)
public class ErrorIgnoringConfiguration extends Configuration {
}
