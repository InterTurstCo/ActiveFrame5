package ru.intertrust.cm.core.config.base;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.ConfigurationConverter;
import ru.intertrust.cm.core.config.converter.LoadedConfigurationConverter;

/**
 * Extends {@link ru.intertrust.cm.core.config.base.Configuration} to provide with error-ignoring
 * {@link org.simpleframework.xml.convert.Converter}
 */
@Root
@Convert(LoadedConfigurationConverter.class)
public class LoadedConfiguration extends Configuration {
}
