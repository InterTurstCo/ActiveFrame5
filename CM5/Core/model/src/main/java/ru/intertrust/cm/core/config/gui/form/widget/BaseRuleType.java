package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

public class BaseRuleType implements Dto {

  @ElementList(name = "rule", inline = true)
  protected List<RuleTypeConfig> ruleTypeConfigs;

  public List<RuleTypeConfig> getRuleTypeConfigs() {
    if(ruleTypeConfigs == null){
      ruleTypeConfigs = new ArrayList<>();
    }
    return ruleTypeConfigs;
  }

  public void setRuleTypeConfigs(List<RuleTypeConfig> ruleTypeConfigs) {
    this.ruleTypeConfigs = ruleTypeConfigs;
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

    BaseRuleType that = (BaseRuleType) o;

    if (ruleTypeConfigs != null ? !ruleTypeConfigs.equals(that.ruleTypeConfigs) : that.ruleTypeConfigs != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (ruleTypeConfigs != null ? ruleTypeConfigs.hashCode() : 0);
    return result;
  }
}
