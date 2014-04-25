package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.model.FatalException;

/**
 * Java class версия фильтра в области поиска.
 * @author atsvetkov
 *
 */
public class JavaClassDomainObjectFilter implements DomainObjectFilter {

    private String javaClass;

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @Override
    public boolean filter(DomainObject object) {
        DomainObjectFilter filter = null;
        try {
            Class<? extends DomainObjectFilter> clazz =
                    (Class<? extends DomainObjectFilter>) Class.forName(javaClass);
            filter = clazz.newInstance();
        } catch (Exception e) {
            throw new FatalException("Error creating JavaClassDomainObjectFilter : " + javaClass, e);
        }

        return filter.filter(object);
    }

}
