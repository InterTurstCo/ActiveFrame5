package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "rule")
public class RuleTypeConfig implements Dto {

  @Attribute(name = "apply-expression",required = false)
  private String applyExpression;

  @Attribute(name = "value",required = false)
  private String value;

  @Attribute(name = "field",required = false)
  private String field;


  public String getApplyExpression() {
    return applyExpression;
  }

  public void setApplyExpression(String applyExpression) {
    this.applyExpression = applyExpression;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    RuleTypeConfig that = (RuleTypeConfig) o;

    if (applyExpression != null ? !applyExpression.equals(that.applyExpression) : that.applyExpression != null) {
      return false;
    }
    if (value != null ? !value.equals(that.value) : that.value != null) {
      return false;
    }
    if (field != null ? !field.equals(that.field) : that.field != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (applyExpression != null ? applyExpression.hashCode() : 0);
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (field != null ? field.hashCode() : 0);
    return result;
  }
}
