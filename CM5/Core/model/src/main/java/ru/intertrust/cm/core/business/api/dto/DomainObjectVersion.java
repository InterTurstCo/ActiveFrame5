package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Интерфейс версии доменного объекта
 * @author larin
 * 
 */
public interface DomainObjectVersion extends IdentifiableObject {

    /**
     * Перечисление операций аудит лога
     * @author larin
     * 
     */
    enum AuditLogOperation {
        CREATE(1),
        UPDATE(2),
        DELETE(3);

        private int operation;

        AuditLogOperation(int operation) {
            this.operation = operation;
        }

        public int getOperation() {
            return operation;
        }
    }
        
    /**
     * Идентификатор доменного объекта
     * @return
     */
    Id getDomainObjectId();

    /**
     * Получение дополнительной информации о версии (зарезервировано)
     * @return
     */
    String getVersionInfo();

    /**
     * Получение информации о компоненте, производившей изменения. Информация
     * берется из systemAccessToken
     * @return
     */
    String getComponent();

    /**
     * Получение IP адреса хоста, с которого выполнялась работа при выполнении
     * изменений
     * @return
     */
    String getIpAddress();

    /**
     * Идентификатор персоны (тип Person) выполнившей изменение
     * @return
     */
    Id getModifier();

    /**
     * Возвращает дату модификации данного доменного объекта
     * 
     * @return дату модификации данного доменного объекта
     */
    Date getModifiedDate();
    
    /**
     * Получение операции выполненной с доменным объектом
     * <li>1 - создание
     * <li>2 - изменение
     * <li>3 - удаление
     * @return
     */
    AuditLogOperation getOperation();

}
