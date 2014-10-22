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
    private PreviewConfig readOnlyPreviewConfig;

    @Element(name="small-preview", required = false)
    private PreviewConfig smallPreviewConfig;

    @Element(name="large-preview", required = false)
    private PreviewConfig largePreviewConfig;

    public PreviewConfig getReadOnlyPreviewConfig() {
        return readOnlyPreviewConfig;
    }

    public PreviewConfig getSmallPreviewConfig() {
        return smallPreviewConfig;
    }

    public PreviewConfig getLargePreviewConfig() {
        return largePreviewConfig;
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
        if (largePreviewConfig != null ? !largePreviewConfig.equals(that.largePreviewConfig) :
                that.largePreviewConfig != null) {
            return false;
        }
        if (readOnlyPreviewConfig != null ? !readOnlyPreviewConfig.equals(that.readOnlyPreviewConfig) :
                that.readOnlyPreviewConfig != null) {
            return false;
        }
        if (smallPreviewConfig != null ? !smallPreviewConfig.equals(that.smallPreviewConfig) :
                that.smallPreviewConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = readOnlyPreviewConfig != null ? readOnlyPreviewConfig.hashCode() : 0;
        result = 31 * result + (smallPreviewConfig != null ? smallPreviewConfig.hashCode() : 0);
        result = 31 * result + (largePreviewConfig != null ? largePreviewConfig.hashCode() : 0);
        return result;
    }
}
