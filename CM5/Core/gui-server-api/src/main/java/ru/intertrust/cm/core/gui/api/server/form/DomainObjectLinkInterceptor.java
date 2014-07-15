package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * Перехватчик событий связывания и разрыва связей доменных объектов с родительскими на форме. Вызывается в момент
 * сохранения и удаления объектов.
 *
 * @author Denis Mitavskiy
 *         Date: 18.06.2014
 *         Time: 10:46
 */
public interface DomainObjectLinkInterceptor extends ComponentHandler {
    /**
     * Данный метод вызывается непосредственно перед тем, как происходит связывание объекта с родительским объектом на форме.
     * Можно выполнить операции модификации и над самим объектом (в данный перехватчик передаётся ссылка
     * на него (context.getLinkedObject()). Если доменный объект изменяется, то эти изменения должны быть отражены в данной ссылке,
     * чтобы механизм форм их не затёр). Связанный объект можно и удалить, но во избежание конфликтов метод должен в этом
     * случае вернуть false, чтобы механизм форм не попытался произвести операции сохранения.
     * @param context контекст связывания объекта с родительским
     * @return результат работы перехватчика события "перед связыванием"
     */
    BeforeLinkResult beforeLink(DomainObjectLinkContext context);

    /**
     * Данный метод вызывается непосредственно перед тем, как происходит разрыв связи объекта с родительским объектом на форме.
     * Можно выполнить операции модификации и над самим объектом (в данный перехватчик передаётся ссылка
     * на него (context.getLinkedObject()). Если доменный объект изменяется, то эти изменения должны быть отражены в данной ссылке,
     * чтобы механизм форм их не затёр). Связанный объект можно и удалить, но во избежание конфликтов метод должен в этом
     * случае вернуть false, чтобы механизм форм не попытался это сделать ещё раз
     * @param context контекст разрыва связи объекта с родительским
     * @return результат работы перехватчика события "перед разрывом связи"
     */
    BeforeUnlinkResult beforeUnlink(DomainObjectLinkContext context);
}
