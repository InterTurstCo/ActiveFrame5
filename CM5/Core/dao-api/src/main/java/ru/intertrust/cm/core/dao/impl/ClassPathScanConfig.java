package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.model.FatalException;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class ClassPathScanConfig {
    private Set<Class<? extends Annotation>> annotationList = new HashSet<>();

    public ClassPathScanConfig(){
    }

    public ClassPathScanConfig(String ... annotationClasses) {
        try {
            for (String annotation : annotationClasses) {
                annotationList.add((Class<? extends Annotation>) getClass().getClassLoader().loadClass(annotation));
            }
        }catch(ClassNotFoundException ex){
            throw new FatalException("Error init ClassPathScanConfig", ex);
        }
    }

    public Set<Class<? extends Annotation>> getAnnotationList(){
        return annotationList;
    }
}
