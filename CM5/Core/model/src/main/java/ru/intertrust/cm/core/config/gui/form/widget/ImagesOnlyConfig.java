package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 21.10.14
 *         Time: 12:41
 */
@Root(name="images-only")
public class ImagesOnlyConfig implements Dto {

    @Element(name="read-only-preview", required = false)
    private PreviewConfig readOnlyPreview;

    @Element(name="small-preview", required = false)
    private PreviewConfig smallPreview;

    @Element(name="large-preview", required = false)
    private PreviewConfig largePreview;

    public PreviewConfig getReadOnlyPreview() {
        return readOnlyPreview;
    }

    public PreviewConfig getSmallPreview() {
        return smallPreview;
    }

    public PreviewConfig getLargePreview() {
        return largePreview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImagesOnlyConfig that = (ImagesOnlyConfig) o;
        if (largePreview != null ? !largePreview.equals(that.largePreview) : that.largePreview != null) {
            return false;
        }
        if (readOnlyPreview != null ? !readOnlyPreview.equals(that.readOnlyPreview) : that.readOnlyPreview != null) {
            return false;
        }
        if (smallPreview != null ? !smallPreview.equals(that.smallPreview) : that.smallPreview != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = readOnlyPreview != null ? readOnlyPreview.hashCode() : 0;
        result = 31 * result + (smallPreview != null ? smallPreview.hashCode() : 0);
        result = 31 * result + (largePreview != null ? largePreview.hashCode() : 0);
        return result;
    }
}
