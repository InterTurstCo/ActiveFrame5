package ru.intertrust.cm.core.business.api.dto;

/**
 * Поисковый фильтр, изменяющий условие фильтрации на обратное.
 * Может быть использован с любым другим поисковым фильтром.
 * 
 * @author apirozhkov
 */
public class NegativeFilter implements SearchFilter {

    private SearchFilter baseFilter;

    public NegativeFilter(SearchFilter baseFilter) {
        this.baseFilter = baseFilter;
    }

    @Override
    public String getFieldName() {
        return baseFilter.getFieldName();
    }

    public SearchFilter getBaseFilter() {
        return baseFilter;
    }
}
