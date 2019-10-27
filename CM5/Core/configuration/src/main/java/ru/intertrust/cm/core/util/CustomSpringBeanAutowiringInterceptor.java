package ru.intertrust.cm.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

public class CustomSpringBeanAutowiringInterceptor extends SpringBeanAutowiringInterceptor {

	private static final Map<BeanFactory, AutowiredAnnotationBeanPostProcessor> postProcessorsByFactoryMap = new ConcurrentHashMap<>();
	
	@Override
	protected void doAutowireBean(Object target) {
		AutowiredAnnotationBeanPostProcessor bpp = postProcessorsByFactoryMap.computeIfAbsent(getBeanFactory(target), factory -> initBeanPostProcessor(factory, target));
		bpp.processInjection(target);
	}

	private AutowiredAnnotationBeanPostProcessor initBeanPostProcessor(final BeanFactory factory, final Object target) {
		
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		configureBeanPostProcessor(bpp, target);
		bpp.setBeanFactory(factory);
		
		return bpp;
	}
}
