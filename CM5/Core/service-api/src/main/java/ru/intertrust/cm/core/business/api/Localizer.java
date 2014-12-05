package ru.intertrust.cm.core.business.api;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 18:03
 */
public interface Localizer {

    public static final String FIELD = "FIELD";
    public static final String DOMAIN_OBJECT = "DOMAIN_OBJECT";
    public static final String SEARCH_AREA = "SEARCH_AREA";
    public static final String SEARCH_DOMAIN_OBJECT = "SEARCH_DOMAIN_OBJECT";
    public static final String SEARCH_FIELD = "SEARCH_FIELD";

    public static final String DOMAIN_OBJECT_CONTEXT = "domain-object-type";

    interface Remote extends Localizer {}

    public void load();

    public String getDisplayText( String value, String classifier, Map<String, ? extends Object> context);

    public String getDisplayText( String value, String classifier);
}
