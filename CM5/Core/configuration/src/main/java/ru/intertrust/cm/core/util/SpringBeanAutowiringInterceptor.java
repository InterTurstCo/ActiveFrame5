package ru.intertrust.cm.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.PostActivate;
import javax.interceptor.InvocationContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;

public class SpringBeanAutowiringInterceptor {

	private static final Map<BeanFactory, AutowiredAnnotationBeanPostProcessor> postProcessorsByFactoryMap = new ConcurrentHashMap<>();

	@PostConstruct
	@PostActivate
	public void autowireBean(InvocationContext invocationContext) {
		doAutowireBean(invocationContext.getTarget());
		try {
			invocationContext.proceed();
		} catch (Exception ex) {
			throw new EJBException(ex);
		}
	}

	protected void doAutowireBean(Object target) {
		AutowiredAnnotationBeanPostProcessor bpp = postProcessorsByFactoryMap.computeIfAbsent(getBeanFactory(target), factory -> initBeanPostProcessor(factory, target));
		bpp.processInjection(target);
	}

	private AutowiredAnnotationBeanPostProcessor initBeanPostProcessor(BeanFactory factory, Object target) {
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		configureBeanPostProcessor(bpp, target);
		bpp.setBeanFactory(factory);

		return bpp;
	}

	protected void configureBeanPostProcessor(AutowiredAnnotationBeanPostProcessor processor, Object target) {
	}

	protected BeanFactory getBeanFactory(Object target) {
		BeanFactory factory = getBeanFactoryByBean(target);
		if (factory instanceof ApplicationContext) {
			factory = ((ApplicationContext) factory).getAutowireCapableBeanFactory();
		}
		return factory;
	}

	protected BeanFactory getBeanFactoryByBean(Object target) {
		String key = getBeanFactoryLocatorKey(target);
		BeanFactory ref = SingletonBeanFactoryLocator.getInstance().getBeanFactory(key);

		return ref;
	}

	protected String getBeanFactoryLocatorKey(Object target) {
		return null;
	}
}
