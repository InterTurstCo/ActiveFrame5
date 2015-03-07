package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.03.2015
 *         Time: 8:47
 */
@ComponentName("default.view.table.action")
public class DefaultViewLinkedTableAction extends LinkedTableAction {
    private LinkedDomainObjectsTableState state;
    @Override
    protected void execute(Id id, int rowIndex) {
        String domainObjectType = rowFormState == null ? null : rowFormState.getRootDomainObjectType();
        LinkedFormDialogBoxBuilder lfb = new LinkedFormDialogBoxBuilder()
                .withEditable(false)
                .withPopupTitlesHolder(state.getPopupTitlesHolder())
                .withLinkedFormMapping(state.getLinkedDomainObjectsTableConfig().getLinkedFormMappingConfig())
                .withTypeTitleMap(state.getTypeTitleMap())
                .withFormState(rowFormState)
                .withObjectType(domainObjectType)
                .withId(id)
                .withHeight(GuiUtil.getModalHeight(domainObjectType, state.getLinkedDomainObjectsTableConfig()))
                .withWidth(GuiUtil.getModalWidth(domainObjectType, state.getLinkedDomainObjectsTableConfig()))
                .buildDialogBox();
        lfb.display();
    }

    public void setState(LinkedDomainObjectsTableState state) {
        this.state = state;
    }

    @Override
    protected String getServerComponentName() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Component createNew() {
        return new DefaultViewLinkedTableAction();
    }
}

