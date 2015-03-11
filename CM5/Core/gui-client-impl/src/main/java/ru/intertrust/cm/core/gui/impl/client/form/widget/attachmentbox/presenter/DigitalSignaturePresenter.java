package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DigitalSignatureConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DigitalSignaturesConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lesia Puhova
 *         Date: 11.03.2015
 *         Time: 15:58
 */
public class DigitalSignaturePresenter  implements AttachmentElementPresenter{

    private final AttachmentElementPresenter presenter;
    private final AttachmentItem item;
    private final Set<String> digitalSignaturesTypes = new HashSet<>();

    public DigitalSignaturePresenter(AttachmentElementPresenter presenter, AttachmentItem item,
                                     DigitalSignaturesConfig digitalSignaturesConfig) {
        this.presenter = presenter;
        this.item = item;
        buildDigitalSignaturesSet(digitalSignaturesConfig);
    }

    @Override
    public Panel presentElement() {
        Panel element = (presenter != null ? presenter.presentElement() : new AbsolutePanel());
        if (isDigitalSignature(item)) {
            ActionLinkConfig actionConfig = new ActionLinkConfig();
            actionConfig.setActionName("verify.signature.attachment.action");
            actionConfig.setText("ЭЦП"); //TODO: localize
            return new ActionPresenter(presenter, actionConfig, item).presentElement();
        }
        return element;
    }

    private void buildDigitalSignaturesSet(DigitalSignaturesConfig digitalSignaturesConfig) {
        if (digitalSignaturesConfig != null) {
            for (DigitalSignatureConfig dsConfig : digitalSignaturesConfig.getDigitalSignatureConfigs()) {
                digitalSignaturesTypes.add(dsConfig.getType());
            }
        }
    }

    private boolean isDigitalSignature(AttachmentItem item) {
        return item.getDomainObjectType() != null && digitalSignaturesTypes.contains(item.getDomainObjectType());
    }
}
