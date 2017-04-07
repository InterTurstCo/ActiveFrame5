package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Commit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 02.10.2014 11:19.
 */
public class BaseActionConfig extends AbstractActionConfig {

    @Attribute(name = "visible-when-new", required = false)
    private boolean visibleWhenNew = true;

    @Attribute(name = "visibility-state-condition", required = false)
    private String visibilityStateCondition;

    @Attribute(name = "visibility-checker", required = false)
    private String visibilityChecker;

    @Attribute(name = "permissions", required = false)
    private String permissions;

    @ElementList(name = "action-params", required = false)
    private List<ActionParamConfig> actionParams;

    private Map<String, String> properties = new HashMap<>();

    @Commit
    public void commit() {
        if (actionParams != null && !actionParams.isEmpty()) {
            for (ActionParamConfig param : actionParams) {
                properties.put(param.getName(), param.getValue());
            }
            //actionParams = null;
        }
    }

    public boolean isVisibleWhenNew() {
        return visibleWhenNew;
    }

    public void setVisibleWhenNew(boolean visibleWhenNew) {
        this.visibleWhenNew = visibleWhenNew;
    }

    public String getVisibilityStateCondition() {
        return visibilityStateCondition;
    }

    public void setVisibilityStateCondition(String visibilityStateCondition) {
        this.visibilityStateCondition = visibilityStateCondition;
    }

    public String getVisibilityChecker() {
        return visibilityChecker;
    }

    public void setVisibilityChecker(String visibilityChecker) {
        this.visibilityChecker = visibilityChecker;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getProperty(final String key) {
        return properties == null ? null : properties.get(key);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseActionConfig that = (BaseActionConfig) o;

        if (visibleWhenNew != that.visibleWhenNew) return false;
        if (visibilityStateCondition != null ? !visibilityStateCondition.equals(that.visibilityStateCondition) : that.visibilityStateCondition != null)
            return false;
        if (visibilityChecker != null ? !visibilityChecker.equals(that.visibilityChecker) : that.visibilityChecker != null)
            return false;
        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null) return false;
        if (actionParams != null ? !actionParams.equals(that.actionParams) : that.actionParams != null) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;

        return true;
    }
}
