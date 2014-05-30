package ru.intertrust.cm.core.business.api.dto;

/**
 * Фильтр для поиска по пустым полям.
 * 
 * @author apirozhkov
 */
public class EmptyValueFilter extends SearchFilterBase {

    public EmptyValueFilter() {
        super();
    }

    public EmptyValueFilter(String fieldName) {
        super(fieldName);
    }

    @Override
    public String toString() {
        return "is empty";
    }
}
