package ru.intertrust.cm.core.business.api.dto;

public interface SearchFilter extends Dto {

    static final String EVERYWHERE = "*";

    String getFieldName();
}
