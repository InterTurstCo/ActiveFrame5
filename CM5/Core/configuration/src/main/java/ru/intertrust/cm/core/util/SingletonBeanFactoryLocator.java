package ru.intertrust.cm.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

public class SingletonBeanFactoryLocator {

	private static final String DEFAULT_RESOURCE_LOCATION = "classpath*:beanRefContext.xml";

	private static final Map<String, SingletonBeanFactoryLocator> instances = new ConcurrentHashMap<>();

	public static SingletonBeanFactoryLocator getInstance() throws BeansException {
		return getInstance(null);
	}

	public static SingletonBeanFactoryLocator getInstance(String selector) throws BeansException {
		String resourceLocation = selector;
		if (resourceLocation == null) {
			resourceLocation = DEFAULT_RESOURCE_LOCATION;
		}

		if (!ResourcePatternUtils.isUrl(resourceLocation)) {
			resourceLocation = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourceLocation;
		}

		SingletonBeanFactoryLocator locator = instances.computeIfAbsent(resourceLocation, SingletonBeanFactoryLocator::new);

		return locator;
	}

	private final Map<String, BeanFactory> bfInstancesByKey = new ConcurrentHashMap<>();

	private final String resourceLocation;

	protected SingletonBeanFactoryLocator(String resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public BeanFactory getBeanFactory(String factoryKey) throws BeansException {
		
		return this.bfInstancesByKey.computeIfAbsent(this.resourceLocation, k -> {

			BeanFactory groupContext = createDefinition(this.resourceLocation, factoryKey);

			initializeDefinition(groupContext);

			final BeanFactory beanFactory;
			if (factoryKey != null) {
				beanFactory = groupContext.getBean(factoryKey, BeanFactory.class);
			} else {
				beanFactory = groupContext.getBean(BeanFactory.class);
			}

			return beanFactory;
		});
	}

	protected BeanFactory createDefinition(String resourceLocation, String factoryKey) {
		return new ClassPathXmlApplicationContext(new String[] { resourceLocation }, false);
	}

	protected void initializeDefinition(BeanFactory groupDef) {
		if (groupDef instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) groupDef).refresh();
			((ConfigurableApplicationContext) groupDef).registerShutdownHook();
		}
	}

	protected void destroyDefinition(BeanFactory groupDef, String selector) {
		if (groupDef instanceof ConfigurableBeanFactory) {
			((ConfigurableBeanFactory) groupDef).destroySingletons();
		}
	}

}
