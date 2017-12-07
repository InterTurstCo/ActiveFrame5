package ru.intertrust.cm.core.gui.impl.server.form;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.title.ExistingObjectConfig;
import ru.intertrust.cm.core.config.gui.form.title.NewObjectConfig;
import ru.intertrust.cm.core.config.gui.form.title.TitleConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.TitleBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.10.2014
 *         Time: 6:41
 */
@ComponentName("title-builder")
public class TitleBuilderImpl implements TitleBuilder {
    @Autowired
    private FormatHandler formatHandler;

    @Override
    public PopupTitlesHolder buildPopupTitles(LinkedFormConfig config, DomainObject root) {
        PopupTitlesHolder result = new PopupTitlesHolder();
        if (config != null && config.getTitleConfig() != null && root != null) {
            fillTitles(config, root, result);
        }
        return result;

    }
    public Map<String, PopupTitlesHolder> buildTypeTitleMap(LinkedFormMappingConfig mappingConfig, DomainObject root) {
        Map<String, PopupTitlesHolder> result = new HashMap<>();
        if (mappingConfig != null) {
            List<LinkedFormConfig> linkedFormConfigs = mappingConfig.getLinkedFormConfigs();
            if (WidgetUtil.isNotEmpty(linkedFormConfigs)) {
                for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                    PopupTitlesHolder popupTitlesHolder = buildPopupTitles(linkedFormConfig, root);
                    String domainObjectType = Case.toLower(linkedFormConfig.getDomainObjectType());
                    result.put(domainObjectType, popupTitlesHolder);
                }
            }
        }
        return result;
    }

    private void fillTitles(LinkedFormConfig config, DomainObject root, PopupTitlesHolder result) {
        TitleConfig titleConfig = config.getTitleConfig();
        String titleNewObject = createTitleNewObject(titleConfig.getNewObjectConfig(), root);
        result.setTitleNewObject(titleNewObject);
        String titleExistingObject = createTitleExistingObject(titleConfig.getExistingObjectConfig(), root);
        result.setTitleExistingObject(titleExistingObject);
    }

    private String createTitleNewObject(NewObjectConfig newObjectConfig, DomainObject root) {
        String displayPattern = newObjectConfig.getPatternConfig().getValue();
        Matcher matcher = FormatHandler.pattern.matcher(displayPattern);
        FormattingConfig formattingConfig = newObjectConfig.getFormattingConfig();
        return formatHandler.format(root, matcher, formattingConfig);
    }

    private String createTitleExistingObject(ExistingObjectConfig existingObjectConfig, DomainObject root) {
        String displayPattern = existingObjectConfig.getPatternConfig().getValue();
        Matcher matcher = FormatHandler.pattern.matcher(displayPattern);
        FormattingConfig formattingConfig = existingObjectConfig.getFormattingConfig();
        return formatHandler.format(root, matcher, formattingConfig);
    }
}
