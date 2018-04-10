package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Серверный обработчик экшена "Сохранить и Закрыть".<br/>
 * Экшен сохранения берется оригинальный, для этой цели данный класс отнаследован от {@link SaveActionHandler}.<br/>
 * Так как все последующие действия после сохранения происходят на клиенте, то работа данного обработчика на сервере ничем не отличается от работы экшена сохранения.<br/>
 * Поэтому данный класс не содержит никакого специфического кода.
 * <p>
 * <p>
 * Created by Myskin Sergey on 06.04.2018.
 */

@ComponentName("save.and.close.action")
public class SaveAndCloseActionHandler extends SaveActionHandler {
}
