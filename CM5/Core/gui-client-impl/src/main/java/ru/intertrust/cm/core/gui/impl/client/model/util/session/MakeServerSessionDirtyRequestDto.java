package ru.intertrust.cm.core.gui.impl.client.model.util.session;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Объект-обертка запроса для "загрязнения" серверной сессии.<br>
 * Никакой информации не передает, необходим только из-за требования сигнатуры<br>
 * <br>
 * Created by Myskin Sergey on 28.07.2020.
 */
public class MakeServerSessionDirtyRequestDto implements Dto {
}
