package ru.intertrust.cm.core.util;

import java.lang.reflect.Field;

/**
 * @author Lesia Puhova
 *         Date: 17.02.2015
 *         Time: 18:27
 */
public interface AnnotationScanCallback {

    public void onAnnotationFound(Object object, Field field) throws IllegalAccessException;
}
