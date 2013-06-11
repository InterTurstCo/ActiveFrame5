package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Фильтр, используемых в основном для отсеивание результатов коллекций
 *
 * Author: Denis Mitavskiy
 * Date: 24.05.13
 * Time: 13:53
 */
public class Filter {
    
    /**
     * содержит имя фильтра
     */
    private String filter;

    private List<String> values = new ArrayList<String>();

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
    

}
