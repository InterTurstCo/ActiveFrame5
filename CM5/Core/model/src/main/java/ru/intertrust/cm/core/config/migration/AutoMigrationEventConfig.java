package ru.intertrust.cm.core.config.migration;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Java модель конфигурации события миграции
 */
public class AutoMigrationEventConfig implements Dto {

    @ElementList(entry="rename-field", inline=true, required = false)
    private List<RenameFieldConfig> renameFieldConfigs = new ArrayList<>();

    @ElementList(entry="change-field-class", inline=true, required = false)
    private List<ChangeFieldClassConfig> changeFieldClassConfigs = new ArrayList<>();

    @ElementList(entry="execute", inline=true, required = false)
    private List<ExecuteConfig> executeConfigs = new ArrayList<>();

    @ElementList(entry="native-command", inline=true, required = false)
    private List<NativeCommandConfig> nativeCommandConfigs = new ArrayList<>();

    @ElementList(entry="create-unique-key", inline=true, required = false)
    private List<CreateUniqueKeyConfig> createUniqueKeyConfigs = new ArrayList<>();

    @ElementList(entry="make-not-null", inline=true, required = false)
    private List<MakeNotNullConfig> makeNotNullConfigs = new ArrayList<>();

    @ElementList(entry="delete-types", inline=true, required = false)
    private List<DeleteTypesConfig> deleteTypesConfigs = new ArrayList<>();

    @ElementList(entry="delete-fields", inline=true, required = false)
    private List<DeleteFieldsConfig> deleteFieldsConfigs = new ArrayList<>();

    @ElementList(entry="unextend-types", inline=true, required = false)
    private List<UnextendTypesConfig> unextendTypesConfigs = new ArrayList<>();

    public List<RenameFieldConfig> getRenameFieldConfigs() {
        return renameFieldConfigs;
    }

    public void setRenameFieldConfigs(List<RenameFieldConfig> renameFieldConfigs) {
        if (renameFieldConfigs == null) {
            this.renameFieldConfigs.clear();
        } else {
            this.renameFieldConfigs = renameFieldConfigs;
        }
    }

    public List<ChangeFieldClassConfig> getChangeFieldClassConfigs() {
        return changeFieldClassConfigs;
    }

    public void setChangeFieldClassConfigs(List<ChangeFieldClassConfig> changeFieldClassConfigs) {
        if (changeFieldClassConfigs == null) {
            this.changeFieldClassConfigs.clear();
        } else {
            this.changeFieldClassConfigs = changeFieldClassConfigs;
        }
    }

    public List<ExecuteConfig> getExecuteConfigs() {
        return executeConfigs;
    }

    public void setExecuteConfigs(List<ExecuteConfig> executeConfigs) {
        if (executeConfigs == null) {
            this.executeConfigs.clear();
        } else {
            this.executeConfigs = executeConfigs;
        }
    }

    public List<NativeCommandConfig> getNativeCommandConfigs() {
        return nativeCommandConfigs;
    }

    public void setNativeCommandConfigs(List<NativeCommandConfig> nativeCommandConfigs) {
        if (nativeCommandConfigs == null) {
            this.nativeCommandConfigs.clear();
        } else {
            this.nativeCommandConfigs = nativeCommandConfigs;
        }
    }

    public List<CreateUniqueKeyConfig> getCreateUniqueKeyConfigs() {
        return createUniqueKeyConfigs;
    }

    public void setCreateUniqueKeyConfigs(List<CreateUniqueKeyConfig> createUniqueKeyConfigs) {
        if (createUniqueKeyConfigs == null) {
            this.createUniqueKeyConfigs.clear();
        } else {
            this.createUniqueKeyConfigs = createUniqueKeyConfigs;
        }
    }

    public List<MakeNotNullConfig> getMakeNotNullConfigs() {
        return makeNotNullConfigs;
    }

    public void setMakeNotNullConfigs(List<MakeNotNullConfig> makeNotNullConfigs) {
        if (makeNotNullConfigs == null) {
            this.makeNotNullConfigs.clear();
        } else {
            this.makeNotNullConfigs = makeNotNullConfigs;
        }
    }

    public List<DeleteTypesConfig> getDeleteTypesConfigs() {
        return deleteTypesConfigs;
    }

    public void setDeleteTypesConfigs(List<DeleteTypesConfig> deleteTypesConfigs) {
        if (deleteTypesConfigs == null) {
            this.deleteTypesConfigs.clear();
        } else {
            this.deleteTypesConfigs = deleteTypesConfigs;
        }
    }

    public List<UnextendTypesConfig> getUnextendTypesConfigs() {
        return unextendTypesConfigs;
    }

    public void setUnextendTypesConfigs(List<UnextendTypesConfig> unextendTypesConfigs) {
        if (unextendTypesConfigs == null) {
            this.unextendTypesConfigs.clear();
        } else {
            this.unextendTypesConfigs = unextendTypesConfigs;
        }
    }

    public List<DeleteFieldsConfig> getDeleteFieldsConfigs() {
        return deleteFieldsConfigs;
    }

    public void setDeleteFieldsConfigs(List<DeleteFieldsConfig> deleteFieldsConfigs) {
        if (deleteFieldsConfigs == null) {
            this.deleteFieldsConfigs.clear();
        } else {
            this.deleteFieldsConfigs = deleteFieldsConfigs;
        }
    }
}
