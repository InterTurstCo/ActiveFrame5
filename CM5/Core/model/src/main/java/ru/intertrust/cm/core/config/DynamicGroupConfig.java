package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Конфигурация динамической группы пользователей. Задаёт, по сути, не конкретную группу, а шаблон вычисляемой группы.
 * Реальные группы будут созданы в БД (таблица GROUP) – по одной на каждый объект заданного типа и статуса, являющийся
 * контекстом для группы.
 * @author atsvetkov
 */
@Root(name = "dynamic-group")
public class DynamicGroupConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Element(name = "context", required = false)
    private ContextConfig context;

    @Element(name = "members", required = false)
    private MembersConfig members;

    @Element(name = "include-group", required = false)
    private IncludeGroupConfig includeGroup;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public MembersConfig getMembers() {
        return members;
    }

    public void setMembers(MembersConfig members) {
        this.members = members;
    }

    public IncludeGroupConfig getIncludeGroup() {
        return includeGroup;
    }

    public void setIncludeGroup(IncludeGroupConfig includeGroup) {
        this.includeGroup = includeGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DynamicGroupConfig that = (DynamicGroupConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (context != null ? !context.equals(that.context) : that.context != null) {
            return false;
        }
        if (members != null ? !members.equals(that.members) : that.members != null) {
            return false;
        }
        if (includeGroup != null ? !includeGroup.equals(that.includeGroup) : that.includeGroup != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return result;
    }

}
