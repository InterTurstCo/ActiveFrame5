package ru.intertrust.cm.core.util;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author Lesia Puhova
 *         Date: 17.02.2015
 *         Time: 16:24
 */
public class AnnotationScanner {

    public static void scanAnnotation(Object object, Class annotationClass, AnnotationScanCallback callback) throws IllegalAccessException {
        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.getType().isPrimitive()) {
                field.setAccessible(true);
                if (field.getAnnotation(annotationClass) != null) {

                    callback.onAnnotationFound(object, field);


                } else if (Collection.class.isAssignableFrom(field.getType())
                        && field.getAnnotation(ElementList.class) != null) {
                    Object obj = field.get(object);
                    if (obj != null) {
                        Collection collection = (Collection) obj;
                        for (Object element : collection) {
                            scanAnnotation(element, annotationClass, callback);
                        }
                    }
                } else if (field.getAnnotation(Element.class) != null) { //may be also Attribute ?
                    Object obj = field.get(object);
                    if (obj != null) {
                        scanAnnotation(obj, annotationClass, callback);
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws IllegalAccessException {
        FormConfig formConfig = new FormConfig();
        WidgetConfigurationConfig widgetConfigurationConfig = new WidgetConfigurationConfig();
        formConfig.setWidgetConfigurationConfig(widgetConfigurationConfig);

        LabelConfig config = new LabelConfig();
        config.setText("hello");

        widgetConfigurationConfig.getWidgetConfigList().add(config);

        scanAnnotation(formConfig, Localizable.class, new AnnotationScanCallback() {
            @Override
            public void onAnnotationFound(Object object, Field field) throws IllegalAccessException{
                field.set(object, "Changed!");
            }
        });
        System.out.println(config.getText());

    }


}
