package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.model.FatalException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 8:50 PM
 */
@Root(name = "domain-object-type")
public class DomainObjectTypeConfig implements TopLevelConfig {

    private Integer id;

    @Attribute(required = true)
    private String name;

    @Attribute(name = "db-id", required = false)
    private Integer dbId;

    @Attribute(name = "initial-status", required = false)
    private String initialStatus;

    @Attribute(name = "extends", required = false)
    private String extendsAttribute;

    @Attribute(name = "audit-log", required = false)
    private Boolean auditLog;

    /**
     * Флаг, указывающий, что это не обычный доменный объект и для него не надо создвавать таблицу. 
     * true - не создать таблицу в базе данных.
     * false - создавать таблицу в базе данных.
     * Значение по умолчанию false
     */
    @Attribute(name = "template", required = false)
    private Boolean isTemplate;

    /**
     * маркер привязки вложенных документов
     */
    @Element(name = "attachment-types", required = false)
    private AttachmentTypesConfig attachmentTypesConfig;

//    @Element(name = "template", required = false)
//    private String template;

    // we can't use a list here directly, as elements inside are different, that's why such a "trick"
    @Element(name = "fields", required = false)
    private DomainObjectFieldsConfig domainObjectFieldsConfig = new DomainObjectFieldsConfig();

    @ElementList(entry="uniqueKey", type=UniqueKeyConfig.class, inline=true, required = false)
    private List<UniqueKeyConfig> uniqueKeyConfigs = new ArrayList<>();

    @Element(name = "indices", required = false)
    private IndicesConfig indicesConfig = new IndicesConfig();

    private static final List<FieldConfig> SYSTEM_FIELDS = new ArrayList<>();

    static {
        initSystemFields();
    }

    public DomainObjectTypeConfig() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    public String getExtendsAttribute() {
        return extendsAttribute;
    }

    public void setExtendsAttribute(String extendsAttribute) {
        this.extendsAttribute = extendsAttribute;
    }

    public DomainObjectFieldsConfig getDomainObjectFieldsConfig() {
        return domainObjectFieldsConfig;
    }

    public void setDomainObjectFieldsConfig(DomainObjectFieldsConfig domainObjectFieldsConfig) {
        this.domainObjectFieldsConfig = domainObjectFieldsConfig;
    }

    public List<FieldConfig> getFieldConfigs() {
        return domainObjectFieldsConfig.getFieldConfigs();
    }

    public List<FieldConfig> getSystemFieldConfigs() {
        return SYSTEM_FIELDS;
    }

    public List<UniqueKeyConfig> getUniqueKeyConfigs() {
        return uniqueKeyConfigs;
    }

    public void setUniqueKeyConfigs(List<UniqueKeyConfig> uniqueKeyConfigs) {
        if(uniqueKeyConfigs != null) {
            this.uniqueKeyConfigs = uniqueKeyConfigs;
        } else {
            this.uniqueKeyConfigs.clear();
        }
    }

    public IndicesConfig getIndicesConfig() {
        return indicesConfig;
    }

    public void setIndicesConfig(IndicesConfig indicesConfig) {
        this.indicesConfig = indicesConfig;
    }

    public Boolean isTemplate() {
        return isTemplate == null ? false : isTemplate;
    }

    public void setTemplate(Boolean template) {
        isTemplate = template;
    }

    public AttachmentTypesConfig getAttachmentTypesConfig() {
        return attachmentTypesConfig;
    }

    public void setAttachmentTypesConfig(AttachmentTypesConfig attachmentTypesConfig) {
        this.attachmentTypesConfig = attachmentTypesConfig;
    }

    private static void initSystemFields() {
        for (SystemField systemField : SystemField.values()) {
            Class<? extends FieldConfig> fieldConfigClass = systemField.getFieldConfigClass();
            FieldConfig systemFieldConfig;
            try {
                systemFieldConfig = fieldConfigClass.newInstance();
            } catch (Exception ex) {
                throw new FatalException("cannot instantiate system field config class");
            }
            systemFieldConfig.setName(systemField.name());
            SYSTEM_FIELDS.add(systemFieldConfig);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectTypeConfig that = (DomainObjectTypeConfig) o;

        if (domainObjectFieldsConfig != null ? !domainObjectFieldsConfig.equals(that.domainObjectFieldsConfig) : that.domainObjectFieldsConfig != null) {
            return false;
        }
        if (extendsAttribute != null ? !extendsAttribute.equals(that.extendsAttribute) : that.extendsAttribute != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        if (dbId != null ? !dbId.equals(that.dbId) : that.dbId != null) {
            return false;
        }

        if (initialStatus != null ? !initialStatus.equals(that.initialStatus) : that.initialStatus != null) {
            return false;
        }
        if (uniqueKeyConfigs != null ? !uniqueKeyConfigs.equals(that.uniqueKeyConfigs) : that.uniqueKeyConfigs != null) {
            return false;
        }
        if (attachmentTypesConfig != null ? !attachmentTypesConfig.equals(that.attachmentTypesConfig) : that.attachmentTypesConfig != null) {
            return false;
        }
        if (auditLog != null ? auditLog != that.auditLog : that.auditLog != null) {
            return false;
        }

        if (indicesConfig != null ? !indicesConfig.equals(that.indicesConfig) : that.indicesConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        //TODO для одинаковых имен конфигураций но обсалютно разном содержании будет одинаковый hash
        return name != null ? name.hashCode() : 0;
    }

    public Boolean isAuditLog() {
        return auditLog;
    }
}
