package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Подключает алгоритм получения доменного объекта, извлекающий из отслеживаемого объекта пользователя, который должен
 * войти в группу. Не нужен, если сам отслеживаемый объект представляет этого пользователя.
 * @author atsvetkov
 */
public class GetPersonConfig implements Dto {
    private static final long serialVersionUID = 5103414225451159378L;

    @Element(name = "doel", required = false)
    private String doel;

    @Element(name = "query", required = false)
    private String query;

    @Element(name = "spring-bean", required = false)
    private String springBean;

    @Element(name = "java-class", required = false)
    private String javaClass;

    public String getDoel() {
        return doel;
    }

    public void setDoel(String doel) {
        this.doel = doel;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSpringBean() {
        return springBean;
    }

    public void setSpringBean(String springBean) {
        this.springBean = springBean;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetPersonConfig that = (GetPersonConfig) o;

        if (doel != null ? !doel.equals(that.doel) : that.doel != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        if (springBean != null ? !springBean.equals(that.springBean) : that.springBean != null) return false;
        if (javaClass != null ? !javaClass.equals(that.javaClass) : that.javaClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = doel != null ? doel.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }
}
