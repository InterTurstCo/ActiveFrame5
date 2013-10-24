package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 24.10.13
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentModel implements Dto {
    private String name;
    private String path;
    private String mimeType;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttachmentModel that = (AttachmentModel) o;

        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
