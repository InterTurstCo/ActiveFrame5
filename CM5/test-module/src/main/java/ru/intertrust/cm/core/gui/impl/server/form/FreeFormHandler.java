package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.server.widget.LinkedDomainObjectFreeFormHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;

@ComponentName("TestFreeFormHandler")
public class FreeFormHandler implements LinkedDomainObjectFreeFormHandler {
  @Override
  public Dto getRowItems(RepresentationRequest representationRequest) {
    return null;
  }
}
