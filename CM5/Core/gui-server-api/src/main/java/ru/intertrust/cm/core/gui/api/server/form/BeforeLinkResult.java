package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Результат перехватчика события связывания доменных объектов
 * @author Denis Mitavskiy
 *         Date: 15.07.2014
 *         Time: 14:02
 */
public class BeforeLinkResult {
    /**
     * true, если обрабатываемый объект необходимо связать с родительским
     */
    public final boolean doLink;

    /**
     * Связываемый доменный объект, после того, как он был обработан перехватчиком
     */
    public final DomainObject linkedDomainObject;

    public BeforeLinkResult(boolean doLink, DomainObject linkedDomainObject) {
        this.doLink = doLink;
        this.linkedDomainObject = linkedDomainObject;
    }
}
