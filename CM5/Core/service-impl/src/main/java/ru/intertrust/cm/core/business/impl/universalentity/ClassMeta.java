package ru.intertrust.cm.core.business.impl.universalentity;

import javax.annotation.Nonnull;

class ClassMeta {

    @Nonnull
    Class<?> clazz;

    @Nonnull
    ClassType clazzType;

    String dopType;

    enum ClassType {
                    ENTITY_NON_ABSTRACT,
                    ENTITY_ABSTRACT,
                    LIST_ELEMENT,
                    COMPONENT;
    }

}