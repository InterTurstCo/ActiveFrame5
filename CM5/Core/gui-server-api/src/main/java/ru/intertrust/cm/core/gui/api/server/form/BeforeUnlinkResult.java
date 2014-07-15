package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Результат перехватчика события разрыва связи между доменными объектов
 * @author Denis Mitavskiy
 *         Date: 15.07.2014
 *         Time: 14:02
 */
public class BeforeUnlinkResult {
    /**
     * true, если обрабатываемый объект необходимо разъединить с родительским
     */
    public final boolean doUnlink;
    /**
     * Связанный доменный объект, после того, как он был обработан перехватчиком
     */
    public final DomainObject unlinkedDomainObject;

    public BeforeUnlinkResult(boolean doUnlink, DomainObject unlinkedDomainObject) {
        this.doUnlink = doUnlink;
        this.unlinkedDomainObject = unlinkedDomainObject;
    }
}
