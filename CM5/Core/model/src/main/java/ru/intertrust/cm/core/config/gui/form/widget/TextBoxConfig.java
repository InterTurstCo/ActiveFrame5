package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:17
 */
@Root(name = "text-box")
public class TextBoxConfig extends WidgetConfig implements Dto {

    @Element(name = "confirmation", required = false)
    private ConfirmationConfig confirmation;

    @Element(name = "confirmation-for", required = false)
    private ConfirmationForConfig confirmationFor;

    public ConfirmationConfig getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(ConfirmationConfig confirmation) {
        this.confirmation = confirmation;
    }

    public ConfirmationForConfig getConfirmationFor() {
        return confirmationFor;
    }

    public void setConfirmationFor(ConfirmationForConfig confirmationFor) {
        this.confirmationFor = confirmationFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TextBoxConfig that = (TextBoxConfig) o;

        if (confirmation != null ? !confirmation.equals(that.confirmation) : that.confirmation != null) return false;
        if (confirmationFor != null ? !confirmationFor.equals(that.confirmationFor) : that.confirmationFor != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (confirmation != null ? confirmation.hashCode() : 0);
        result = 31 * result + (confirmationFor != null ? confirmationFor.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "text-box";
    }
}
