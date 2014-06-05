package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.10.13
 *         Time: 19:09
 */
@Root(name = "attachment-box")
public class AttachmentBoxConfig extends WidgetConfig {

    @Element(name = "attachment-type-ref")
    private AttachmentTypeRefConfig attachmentType;

    @Element(name = "scanner")
    private ScannerConfig scanner;

    @Element(name = "selection-style")
    private SelectionStyleConfig selectionStyle;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Element(name = "action-link", required = false)
    private ActionLinkConfig actionLinkConfig;

    @Element(name = "accepted-types", required = false)
    private AcceptedTypesConfig acceptedTypesConfig;

    public AttachmentBoxConfig() {
        singleChoice = new SingleChoiceConfig();
        singleChoice.setSingleChoice(false);
    }

    public AttachmentTypeRefConfig getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentTypeRefConfig attachmentType) {
        this.attachmentType = attachmentType;
    }

    public ScannerConfig getScanner() {
        return scanner;
    }

    public void setScanner(ScannerConfig scanner) {
        this.scanner = scanner;
    }

    public SelectionStyleConfig getSelectionStyle() {
        return selectionStyle;
    }

    public void setSelectionStyle(SelectionStyleConfig selectionStyle) {
        this.selectionStyle = selectionStyle;
    }

    public SingleChoiceConfig getSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(SingleChoiceConfig singleChoice) {
        this.singleChoice = singleChoice;
    }

    public ActionLinkConfig getActionLinkConfig() {
        return actionLinkConfig;
    }

    public void setActionLinkConfig(ActionLinkConfig actionLinkConfig) {
        this.actionLinkConfig = actionLinkConfig;
    }

    public AcceptedTypesConfig getAcceptedTypesConfig() {
        return acceptedTypesConfig;
    }

    public void setAcceptedTypesConfig(AcceptedTypesConfig acceptedTypesConfig) {
        this.acceptedTypesConfig = acceptedTypesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AttachmentBoxConfig that = (AttachmentBoxConfig) o;

        if (actionLinkConfig != null ? !actionLinkConfig.equals(that.actionLinkConfig) : that.actionLinkConfig != null) return false;
        if (attachmentType != null ? !attachmentType.equals(that.attachmentType) : that.attachmentType != null)
            return false;
        if (scanner != null ? !scanner.equals(that.scanner) : that.scanner != null) return false;
        if (selectionStyle != null ? !selectionStyle.equals(that.selectionStyle) : that.selectionStyle != null)
            return false;
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) : that.singleChoice != null) return false;
        if (acceptedTypesConfig != null ? !acceptedTypesConfig.equals(that.acceptedTypesConfig) : that.
                acceptedTypesConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (attachmentType != null ? attachmentType.hashCode() : 0);
        result = 31 * result + (scanner != null ? scanner.hashCode() : 0);
        result = 31 * result + (selectionStyle != null ? selectionStyle.hashCode() : 0);
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (actionLinkConfig != null ? actionLinkConfig.hashCode() : 0);
        result = 31 * result + (acceptedTypesConfig != null ? acceptedTypesConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "attachment-box";
    }
}
