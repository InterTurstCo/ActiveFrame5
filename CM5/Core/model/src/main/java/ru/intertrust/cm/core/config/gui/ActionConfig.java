package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:02
 */
@Root(name="action")
public class ActionConfig implements TopLevelConfig {

    @Attribute
    private String name;

    @Attribute
    private String component;

    @Attribute
    private String text;

    @Attribute(name="image")
    private String imageUrl;

    @Attribute(name="short-desc", required = false)
    private String shortDesc;

    @Attribute(name = "toggle", required = false)
    private boolean toggle;

    @Attribute(name = "validate", required = false)
    private boolean validate = false;

    private int order;

    private boolean rightSide;

    @Element(name="action-settings")
    @Convert(ActionSettingsConverter.class)
    private ActionSettings actionSettings;

    @Element(name="before-execution")
    private BeforeExecutionConfig beforeExecution;

    @Element(name="after-execution")
    private AfterExecutionConfig afterExecution;

    public ActionConfig() {
    }

    public ActionConfig(String name) {
        this.name = name;
    }

    public ActionConfig(String name, String component) {
        this.name = name;
        this.component = component;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(final boolean checked) {
        this.toggle = checked;
    }

    public int getOrder() {
        return (order > 0) ? order : Integer.MAX_VALUE;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isRightSide() {
        return rightSide;
    }

    public void setRightSide(boolean rightSide) {
        this.rightSide = rightSide;
    }

    public ActionSettings getActionSettings() {
        return actionSettings;
    }

    public void setActionSettings(ActionSettings actionSettings) {
        this.actionSettings = actionSettings;
    }

    @Override
    public String toString() {
        return "ActionConfig {" +
                "name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public BeforeExecutionConfig getBeforeExecution() {
        return beforeExecution;
    }

    public void setBeforeExecution(BeforeExecutionConfig beforeExecution) {
        this.beforeExecution = beforeExecution;
    }

    public AfterExecutionConfig getAfterExecution() {
        return afterExecution;
    }

    public void setAfterExecution(AfterExecutionConfig afterExecution) {
        this.afterExecution = afterExecution;
    }

    public List<ValidatorConfig> getValidatorConfigs() {
        List<ValidatorConfig> validatorConfigs = new ArrayList<ValidatorConfig>();
        BeforeExecutionConfig beforeExecution = getBeforeExecution();
        if (beforeExecution != null) {
            ValidatorsConfig validatorsConfig = beforeExecution.getValidatorsConfig();
            if (validatorsConfig != null) {
                validatorConfigs.addAll(validatorsConfig.getValidators());
            }
        }
        return validatorConfigs;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
