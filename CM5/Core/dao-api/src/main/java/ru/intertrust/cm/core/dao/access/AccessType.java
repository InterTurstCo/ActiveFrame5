package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Тип доступа к объекту системы.
 * <p>Предназначен для использования в маркерах доступа (см. {@link AccessToken}).
 * <p>Реализующие классы обязаны обеспечивать корректную реализацию метода equals(Object).
 * 
 * @author apirozhkov
 */
public interface AccessType extends Dto {

}
